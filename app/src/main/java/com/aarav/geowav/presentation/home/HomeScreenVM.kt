package com.aarav.geowav.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.permissions.GeoPermissionCoordinator
import com.aarav.geowav.core.permissions.GeoPermissionUiState
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.core.utils.ViewerLocationState
import com.aarav.geowav.core.utils.failure
import com.aarav.geowav.core.utils.formatRemainingForEmergency
import com.aarav.geowav.core.utils.messageFor
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.CircleActivityItem
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.GeoConnection
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.model.StayPoint
import com.aarav.geowav.data.model.User
import com.aarav.geowav.data.model.UserPath
import com.aarav.geowav.data.repository.CircleActivityFeedRepository
import com.aarav.geowav.data.repository.PlaceRepositoryImpl
import com.aarav.geowav.domain.repository.CircleRepository
import com.aarav.geowav.domain.repository.ViewerLocationRepository
import com.aarav.geowav.domain.repository.LiveLocationSharingRepository
import com.aarav.geowav.domain.repository.LocationPermissionRepository
import com.aarav.geowav.domain.repository.EmergencySharingRepository
import com.aarav.geowav.platform.LocationManager
import com.aarav.geowav.presentation.components.AwarenessSnapshotUiState
import com.aarav.geowav.presentation.components.LatestActivity
import android.location.Location
import android.location.Address
import android.location.Geocoder
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeScreenVM @Inject constructor(
    private val googleSignInClient: GoogleSignInClient,
    private val placeRepository: PlaceRepositoryImpl,
    private val circleActivityFeedRepository: CircleActivityFeedRepository,
    private val circleRepository: CircleRepository,
    private val viewerLocationRepository: ViewerLocationRepository,
    private val permissionCoordinator: GeoPermissionCoordinator,
    private val locationManager: LocationManager,
    private val liveLocationSharingRepository: LiveLocationSharingRepository,
    private val locationPermissionRepository: LocationPermissionRepository,
    private val emergencySharingRepository: EmergencySharingRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState: MutableStateFlow<HomeScreenUiState> =
        MutableStateFlow(HomeScreenUiState())
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    private val _locations = MutableStateFlow<Map<String, ViewerLocationState>>(emptyMap())
    val locations = _locations.asStateFlow()

    private val _userPaths = MutableStateFlow<Map<String, UserPath>>(emptyMap())
    val userPaths = _userPaths.asStateFlow()

    private var pathJob: Job? = null

    var hasShownWelcome = false
    private val _liveStayPoints = MutableStateFlow<Map<String, List<StayPoint>>>(emptyMap())
    val liveStayPoints: StateFlow<Map<String, List<StayPoint>>> = _liveStayPoints.asStateFlow()

    val viewerId: String
        get() = googleSignInClient.getUserId()

    private val observerJobs = mutableMapOf<String, Job>()

    fun fetchUser() {
        viewModelScope.launch {
            googleSignInClient.currentUser()
                .collect {
                    _uiState.update { state ->
                        state.copy(
                            currentUser = it
                        )
                    }
                }
        }
    }

    fun observeUsers() {

        val lovedOnes = _uiState.value.lovedOnes

        lovedOnes.forEach { member ->

            val userId = member.id

            if (observerJobs.contains(userId)) return@forEach

            val job = viewModelScope.launch {

                viewerLocationRepository
                    .observeUserLocation(userId, viewerId)
                    .collect { viewerState ->

                        when (viewerState) {

                            is ViewerLocationState.NormalSharing -> {

                                _uiState.update {
                                    it.copy(
                                        currentSessionParticipants =
                                            viewerState.location.sharedWith
                                    )
                                }

                                _locations.update {
                                    it + (userId to viewerState)
                                }

                                _userPaths.update {
                                    it + (
                                            userId to UserPath(
                                                startedAt = viewerState.location.startedAt,
                                                points = viewerState.path,
                                                isActive = true
                                            )
                                            )
                                }

                                _liveStayPoints.update {
                                    it + (userId to viewerState.stayPoints)
                                }
                            }

                            is ViewerLocationState.EmergencySharing -> {

                                _uiState.update {
                                    it.copy(
                                        currentSessionParticipants =
                                            viewerState.location.sharedWith,
                                        remainingTime = formatRemainingForEmergency(viewerState.endsAt)
                                    )
                                }

                                _locations.update {
                                    it + (userId to viewerState)
                                }

                                _userPaths.update {
                                    it + (
                                            userId to UserPath(
                                                startedAt = viewerState.location.startedAt,
                                                points = viewerState.path,
                                                isActive = true
                                            )
                                            )
                                }

                                _liveStayPoints.update {
                                    it + (userId to viewerState.stayPoints)
                                }
                            }

                            ViewerLocationState.Blocked -> {

                                _locations.update { it - userId }
                                _userPaths.update { it - userId }
                                _liveStayPoints.update { it - userId }
                            }
                        }
                    }
            }

            observerJobs[userId] = job
        }
    }

    fun cleanupRemovedUsers(activeUserIds: Set<String>) {
        observerJobs.keys
            .filter { it !in activeUserIds }
            .forEach { userId ->
                observerJobs.remove(userId)?.cancel()

                _locations.update {
                    it - userId
                }

                _liveStayPoints.update {
                    it - userId
                }
            }
    }

    val allPlaces = placeRepository.getPlaces()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val awarenessItems = circleActivityFeedRepository.observeRecentActivity()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val permissionStateFlow = permissionCoordinator.state

    private val userLocationFlow: Flow<Location?> = permissionStateFlow
        .flatMapLatest { state ->
            if (state.foregroundLocationGranted) {
                locationManager.getLocationUpdates()
                    .map { it as Location? }
                    .onStart { emit(null) }
            } else {
                flowOf(null)
            }
        }
        .onStart { emit(null) }

    private val geocodedAddressFlow: Flow<String?> = userLocationFlow
        .distinctUntilChanged { old, new ->
            if (old == null && new == null) true
            else if (old != null && new != null) {
                old.distanceTo(new) < 10f
            } else false
        }
        .flatMapLatest { location ->
            if (location == null) {
                flowOf<String?>(null)
            } else {
                flow {
                    val address = resolveApproximateAddress(location)
                    emit(address)
                }
            }
        }
        .onStart { emit(null) }

    private val isEmergencyFlow: Flow<Boolean> = googleSignInClient.getUserIdFlow()
        .flatMapLatest { uid ->
            if (uid.isEmpty()) flowOf(false)
            else emergencySharingRepository.observeEmergency(uid).map { it != null }
        }
        .onStart { emit(false) }

    private val isSharingFlow: Flow<Boolean> = googleSignInClient.getUserIdFlow()
        .flatMapLatest { uid ->
            if (uid.isEmpty()) flowOf(false)
            else liveLocationSharingRepository.observeSharingActive(uid)
        }
        .onStart { emit(false) }

    private val sharedWithFlow: Flow<Set<String>> = googleSignInClient.getUserIdFlow()
        .flatMapLatest { uid ->
            if (uid.isEmpty()) flowOf(emptySet())
            else locationPermissionRepository.getAllowedViewers(uid)
        }
        .onStart { emit(emptySet()) }

    private val timeTickerFlow: Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(30_000)
        }
    }.onStart { emit(System.currentTimeMillis()) }

    private fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val minutes = diff / 60000
        val hours = diff / (60000 * 60)
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min${if (minutes > 1) "s" else ""} ago"
            hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
            else -> {
                val df = java.text.SimpleDateFormat("dd MMMM y", java.util.Locale.getDefault())
                df.format(java.util.Date(timestamp))
            }
        }
    }

    val awarenessSnapshotUiState: StateFlow<AwarenessSnapshotUiState> = combine(
        allPlaces,
        userLocationFlow,
        isSharingFlow,
        isEmergencyFlow,
        sharedWithFlow,
        _uiState.map { it.lovedOnes }.distinctUntilChanged(),
        awarenessItems,
        permissionStateFlow,
        timeTickerFlow,
        geocodedAddressFlow
    ) { args ->
        val places = args[0] as List<Place>
        val location = args[1] as Location?
        val isSharing = args[2] as Boolean
        val isEmergency = args[3] as Boolean
        val sharedWith = args[4] as Set<String>
        val lovedOnes = args[5] as List<CircleMember>
        val activities = args[6] as List<CircleActivityItem>
        val permissionState = args[7] as GeoPermissionUiState
        val geocodedAddress = args[9] as String?

        val currentPlace = when {
            !permissionState.foregroundLocationGranted -> "Location access needed"
            location == null -> "Away from saved places"
            else -> {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                val insidePlace = places.firstOrNull { place ->
                    val placeLatLng = LatLng(place.latitude, place.longitude)
                    val distance = SphericalUtil.computeDistanceBetween(currentLatLng, placeLatLng)
                    distance <= place.radius
                }
                insidePlace?.customName?.ifEmpty { insidePlace.placeName } ?: geocodedAddress?.let { "📍$it" } ?: "Away from saved places"
            }
        }

        val visibleMembers = if (isEmergency) {
            lovedOnes
        } else {
            lovedOnes.filter { it.id in sharedWith }
        }

        val latestActivity = activities.firstOrNull { it.actorId != viewerId }?.let { newest ->
            val isArrival = newest.normalizedTransitionType == "ARRIVED"
            LatestActivity(
                actorName = newest.actorName,
                actorAvatar = newest.actorAvatar,
                placeName = newest.placeName,
                isArrival = isArrival,
                relativeTime = getRelativeTime(newest.timestamp)
            )
        }

        AwarenessSnapshotUiState(
            currentPlace = currentPlace,
            isSharing = isSharing,
            isEmergency = isEmergency,
            visibleMembers = visibleMembers,
            latestActivity = latestActivity,
            totalLovedOnesCount = lovedOnes.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AwarenessSnapshotUiState(
            currentPlace = "Away from saved places",
            isSharing = false,
            isEmergency = false,
            visibleMembers = emptyList(),
            latestActivity = null,
            totalLovedOnesCount = 0
        )
    )

    fun loadLovedOnes() {
        if (viewerId.isEmpty()) return

        _uiState.update {
            it.copy(
                isLovedOnesLoading = true,
                lovedOnesError = null
            )
        }

        viewModelScope.launch {
            when (val result =
                circleRepository.getAcceptedLovedOnes(viewerId)
            ) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            lovedOnes = result.data ?: emptyList(),
                            isLovedOnesLoading = false,
                            lovedOnesError = null
                        )
                    }
                }

                is Resource.NoInternet,
                is Resource.Timeout,
                is Resource.ServerError,
                is Resource.UnknownError,
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLovedOnesLoading = false,
                            lovedOnesError = result.failure.messageFor("your circle", result.message)
                        )
                    }
                }

                is Resource.Loading -> Unit
            }
        }
    }

    fun fetchViewerInfo() {
        viewModelScope.launch {

            val activeViewerIds = locations.value
                .filterValues {
                    it is ViewerLocationState.NormalSharing ||
                            it is ViewerLocationState.EmergencySharing
                }
                .keys

            // Remove viewers that are no longer active
            _uiState.update { state ->
                state.copy(
                    currentViewers = state.currentViewers
                        .filter { it.userId in activeViewerIds }
                )
            }

            // Add new active viewers
            activeViewerIds.forEach { id ->
                val user = googleSignInClient.findUserByUserId(id)

                if (user != null &&
                    !_uiState.value.currentViewers.contains(user)
                ) {
                    _uiState.update {
                        it.copy(
                            currentViewers = it.currentViewers + user
                        )
                    }
                }
            }
        }
    }


    private var activityObserveJob: Job? = null

    private fun observeRecentActivityFeed(uid: String) {
        activityObserveJob?.cancel()

        _uiState.update {
            it.copy(
                isAwarenessLoading = true,
                awarenessError = null
            )
        }

        activityObserveJob = viewModelScope.launch {
            val flow = circleActivityFeedRepository.observeRecentActivity()
            flow.collect { activities ->
                _uiState.update {
                    it.copy(
                        awarenessItems = activities,
                        isAwarenessLoading = false,
                        awarenessError = null
                    )
                }
            }
        }
    }

    init {
        // Pre-populate user from FirebaseAuth immediately if available
        val firebaseUser = googleSignInClient.firebaseAuth.currentUser
        if (firebaseUser != null) {
            val fallbackUser = User(
                userId = firebaseUser.uid,
                username = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "User",
                email = firebaseUser.email ?: ""
            )
            _uiState.update { state ->
                state.copy(
                    currentUser = fallbackUser,
                    username = fallbackUser.username
                )
            }
        }

        fetchUser()

        viewModelScope.launch {
            placeRepository.getPlaces().collect { places ->
                _uiState.update {
                    it.copy(
                        placesList = places,
                        isPlacesLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            googleSignInClient.getUserIdFlow()
                .distinctUntilChanged()
                .collectLatest { uid ->
                    if (uid.isNotEmpty()) {
                        loadLovedOnes()
                        observeRecentActivityFeed(uid)
                    }
                }
        }

        getUserProfile()
        observePermissionState()
    }

    fun refreshPermissionState() {
        _uiState.update {
            it.copy(permissionState = permissionCoordinator.refresh())
        }
    }

    private fun observePermissionState() {
        refreshPermissionState()
        viewModelScope.launch {
            permissionCoordinator.state.collect { permissionState ->
                _uiState.update {
                    it.copy(permissionState = permissionState)
                }
            }
        }
    }

    fun drawAnimatedPath(
        path: List<LatLng>
    ) {

        if (path.size <= _uiState.value.playbackIndex + 1) return

        pathJob?.cancel()

        pathJob = viewModelScope.launch {
            for (i in _uiState.value.playbackIndex until path.size - 1) {

                val start = _uiState.value.lastPosition ?: path[i]
                val end = path[i + 1]

                val distance = SphericalUtil.computeDistanceBetween(start, end)
                val speed = _uiState.value.speed
                val duration = (distance / (35.0 * speed) * 1000).toLong()

                val steps = (duration / 16).toInt().coerceAtLeast(1)

                for (step in 0..steps) {
                    val fraction = step / steps.toFloat()
                    val interpolated = SphericalUtil.interpolate(start, end, fraction.toDouble())

                    _uiState.update { current ->
                        current.copy(
                            animatedPath = current.animatedPath + interpolated,
                            lastPosition = interpolated,
                            playbackIndex = i
                        )
                    }

                    delay(duration / steps)
                }


                _uiState.update {
                    it.copy(playbackIndex = i + 1)
                }
            }
        }
    }

    fun updateLastLocation(
        location: LatLng
    ) {
        _uiState.update {
            it.copy(
                lastPosition = location
            )
        }   
    }

    fun setPlaybackIndex(index: Int) {
        _uiState.update {
            it.copy(playbackIndex = index)
        }
    }

    fun resetAnimatedPath() {
        _uiState.update {
            it.copy(
                animatedPath = emptyList(),
                playbackIndex = 0,
                lastPosition = null
            )
        }
    }


    fun getUserProfile() {
        Log.i("PROFILE", "fetch profile")
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    username = googleSignInClient.getUserName(),
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pathJob?.cancel()

        Log.i("HOME", "cleared")
    }

    fun signOut() {
        observerJobs.values.forEach { it.cancel() }
        observerJobs.clear()
        _liveStayPoints.value = emptyMap()

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                googleSignInClient.signOut()
            }
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun resolveApproximateAddress(location: Location): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) return@withContext null

                val geocoder = Geocoder(context, java.util.Locale.getDefault())
                val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    ?.firstOrNull()

                address?.toApproximateLabel()
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun Address.toApproximateLabel(): String? {
        val parts = listOfNotNull(
            subLocality,
            locality,
            subAdminArea,
            adminArea
        )
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        return parts.take(2).joinToString(", ").ifBlank {
            getAddressLine(0)?.trim()?.takeIf { it.isNotBlank() }
        }
    }

}

data class HomeScreenUiState(
    val placesList: List<Place> = emptyList(),
    val connectionsList: List<GeoConnection> = emptyList(),
    val lovedOnes: List<CircleMember> = emptyList(),
    val currentViewers: List<User> = emptyList(),
    val currentSessionParticipants: List<String> = emptyList(),
    val animatedPath: List<LatLng> = emptyList(),
    val lastPosition: LatLng? = null,
    val playbackIndex: Int = 0,
    val speed: Float = 1f,
    val awarenessItems: List<CircleActivityItem> = emptyList(),
    val currentUser: User? = null,
    val userAvatar: String? = null,
    val username: String? = null,
    val viewerState: ViewerLocationState? = ViewerLocationState.Blocked,
    val remainingTime: String? = null,
    val permissionState: GeoPermissionUiState = GeoPermissionUiState(),
    val isLovedOnesLoading: Boolean = true,
    val isPlacesLoading: Boolean = true,
    val isAwarenessLoading: Boolean = true,
    val lovedOnesError: String? = null,
    val awarenessError: String? = null
)
