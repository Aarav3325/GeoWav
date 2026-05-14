package com.aarav.geowav.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.permissions.GeoPermissionCoordinator
import com.aarav.geowav.core.permissions.GeoPermissionUiState
import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.core.utils.ViewerLocationState
import com.aarav.geowav.core.utils.formatRemainingForEmergency
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.GeoAlert
import com.aarav.geowav.data.model.GeoConnection
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.model.StayPoint
import com.aarav.geowav.data.model.User
import com.aarav.geowav.data.model.UserPath
import com.aarav.geowav.data.repository.GeoActivityRepositoryImpl
import com.aarav.geowav.data.repository.PlaceRepositoryImpl
import com.aarav.geowav.domain.repository.CircleRepository
import com.aarav.geowav.domain.repository.ViewerLocationRepository
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeScreenVM @Inject constructor(
    private val googleSignInClient: GoogleSignInClient,
    private val placeRepository: PlaceRepositoryImpl,
    private val geoActivityRepositoryImpl: GeoActivityRepositoryImpl,
    private val circleRepository: CircleRepository,
    private val viewerLocationRepository: ViewerLocationRepository,
    private val permissionCoordinator: GeoPermissionCoordinator,
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

    val viewerId = googleSignInClient.getUserId()

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

    val alerts = geoActivityRepositoryImpl.observeAlerts(ActivityFilter.Today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun loadLovedOnes() {
        if (viewerId.isEmpty()) return

        viewModelScope.launch {


            when (val result =
                circleRepository.getAcceptedLovedOnes(viewerId)
            ) {
                is Resource.Success -> {
                    _uiState.update {

                        it.copy(
                            lovedOnes = result.data ?: emptyList(),
                        )
                    }
                }

                else -> {}
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


    init {
        fetchUser()

        viewModelScope.launch {
            combine(allPlaces, alerts) { p, a ->
                Pair(p, a)
            }.collect { (places, alerts) ->
                _uiState.update {
                    it.copy(
                        placesList = places,
                        alertsList = alerts
                    )
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
    val alertsList: List<GeoAlert> = emptyList(),
    val currentUser: User? = null,
    val userAvatar: String? = null,
    val username: String? = null,
    val viewerState: ViewerLocationState? = ViewerLocationState.Blocked,
    val remainingTime: String? = null,
    val permissionState: GeoPermissionUiState = GeoPermissionUiState()
)
