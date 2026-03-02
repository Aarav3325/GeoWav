package com.aarav.geowav.presentation.home

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.tracking.StayPointTracker
import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.core.utils.LiveLocationState
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.core.utils.ViewerLocationState
import com.aarav.geowav.core.utils.formatRemainingForEmergency
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.GeoAlert
import com.aarav.geowav.data.model.GeoConnection
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.model.SessionHistory
import com.aarav.geowav.data.model.StayPoint
import com.aarav.geowav.data.model.User
import com.aarav.geowav.data.model.UserPath
import com.aarav.geowav.data.model.toUserPathLatLng
import com.aarav.geowav.data.repository.GeoActivityRepositoryImpl
import com.aarav.geowav.data.repository.GeoConnectionRepositoryImpl
import com.aarav.geowav.data.repository.PlaceRepositoryImpl
import com.aarav.geowav.domain.repository.CircleRepository
import com.aarav.geowav.domain.repository.LiveLocationSharingRepository
import com.aarav.geowav.domain.repository.SessionHistoryRepository
import com.aarav.geowav.domain.repository.ViewerLocationRepository
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class HomeScreenVM @Inject constructor(
    @ApplicationContext val context: Context,
    private val googleSignInClient: GoogleSignInClient,
    private val connectionRepository: GeoConnectionRepositoryImpl,
    private val placeRepository: PlaceRepositoryImpl,
    private val geoActivityRepositoryImpl: GeoActivityRepositoryImpl,
    private val circleRepository: CircleRepository,
    private val viewerLocationRepository: ViewerLocationRepository,
    private val sessionHistoryRepository: SessionHistoryRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<HomeScreenUiState> =
        MutableStateFlow(HomeScreenUiState())
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    private val _locations = MutableStateFlow<Map<String, ViewerLocationState>>(emptyMap())
    val locations = _locations.asStateFlow()

    private val _userPaths = MutableStateFlow<Map<String, UserPath>>(emptyMap())
    val userPaths = _userPaths.asStateFlow()

    // Stay Point Tracking
    // Each user we observe gets their own StayPointTracker
    private val stayTrackers = mutableMapOf<String, StayPointTracker>()

    // The UI (LiveLocationListener) observes this to draw orange stay markers on the map
    // e.g. {"user_abc" -> [StayPoint(coffee shop), StayPoint(park)]}
    private val _liveStayPoints = MutableStateFlow<Map<String, List<StayPoint>>>(emptyMap())
    val liveStayPoints: StateFlow<Map<String, List<StayPoint>>> = _liveStayPoints.asStateFlow()

//    val userSessionHistory = sessionHistoryRepository.getSessionsForUser("7sZTZoNLRpUBcJSevQJyNq2XRVw1")
//        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList<SessionHistory>())

    val viewerId = googleSignInClient.getUserId()

//    private var countdownJob: Job? = null

    private val observerJobs = mutableMapOf<String, Job>()


    // Observe user live location updates
    fun observeUsers() {


        val lovedOnes = _uiState.value.lovedOnes

        Log.i("OBSERVE", "user: ${lovedOnes}")

        lovedOnes.forEach {
            val userId = it.id


            if (observerJobs.contains(userId)) return

            val job = viewModelScope.launch {
                viewerLocationRepository.observeUserLocation(userId, viewerId)
                    .collect { viewerState ->
//
//                        val sharedAudience  = locations.value
//
//                        Log.i("SHARED", "audience: $sharedAudience")

                        when (viewerState) {

                            is ViewerLocationState.NormalSharing -> {

                                _uiState.update {
                                    it.copy(
                                        currentSessionParticipants = viewerState.location.sharedWith
                                    )
                                }

                                val lat = viewerState.location.lat
                                val lng = viewerState.location.lng

                                if (!isValidLocation(lat, lng)) {
                                    Log.d("LOCATION_SKIP", "Skipped invalid: $lat, $lng")
                                    return@collect
                                }

                                val newPoint = LatLng(lat, lng)

                                _locations.update {
                                    it + (userId to viewerState)
                                }

                                // Stay point tracking
                                feedStayTracker(userId, lat, lng, viewerState.location.timestamp)

                                // Update user path for drawing user path on map using polyline
                                _userPaths.update { current ->

                                    // Get user's path if it exists
                                    val existingPath = current[userId]

                                    /* Remove old entry in case sharing is not active
                                       so that old path is not drawn on map
                                     */
                                    val cleanedPaths =
                                        if (existingPath?.isActive == false)
                                            current - userId
                                        else
                                            current

                                    // Get existing points if any
                                    val existingPoints =
                                        cleanedPaths[userId]?.points.orEmpty()

                                    // Add new point to existing path
                                    val updatedPoints =
                                        if (existingPoints.isNotEmpty()) {
                                            val last = existingPoints.last()
                                            val distance =
                                                SphericalUtil.computeDistanceBetween(last, newPoint)

                                            // Check if distance between points is less than 5 meters
                                            // so that updates are not too frequent
                                            if (distance < 5)
                                                existingPoints
                                            else
                                                existingPoints + newPoint
                                        } else {
                                            listOf(newPoint)
                                        }

                                    // Update user path with new points
                                    cleanedPaths + (
                                            userId to UserPath(
                                                startedAt = viewerState.location.startedAt,
                                                points = updatedPoints,
                                                isActive = true
                                            )
                                            )
                                }
                            }

                            is ViewerLocationState.EmergencySharing -> {


                                _uiState.update {
                                    it.copy(
                                        currentSessionParticipants = viewerState.location.sharedWith
                                    )
                                }


                                val lat = viewerState.location.lat
                                val lng = viewerState.location.lng

                                if (!isValidLocation(lat, lng)) {
                                    Log.d("LOCATION_SKIP", "Skipped invalid: $lat, $lng")
                                    return@collect
                                }

                                val newPoint = LatLng(lat, lng)

                                _locations.update {
                                    it + (userId to viewerState)
                                }

                                // Stay point tracking
                                feedStayTracker(userId, lat, lng, viewerState.location.timestamp)

                                _userPaths.update { current ->

                                    val existingPath = current[userId]

                                    val cleanedPaths =
                                        if (existingPath?.isActive == false)
                                            current - userId
                                        else
                                            current

                                    val existingPoints =
                                        cleanedPaths[userId]?.points.orEmpty()

                                    val updatedPoints =
                                        if (existingPoints.isNotEmpty()) {
                                            val last = existingPoints.last()
                                            val distance =
                                                SphericalUtil.computeDistanceBetween(last, newPoint)

                                            if (distance < 5)
                                                existingPoints
                                            else
                                                existingPoints + newPoint
                                        } else {
                                            listOf(newPoint)
                                        }

                                    cleanedPaths + (
                                            userId to UserPath(
                                                startedAt = viewerState.location.startedAt,
                                                points = updatedPoints,
                                                isActive = true
                                            )
                                            )
                                }
                            }

                            ViewerLocationState.Blocked -> {


                                // Session ended - get all stay points before saving to Firebase
                                // This captures any stay the user is still in + all past stays
                                val finalStayPoints = finalizeStayTracker(userId)

                                Log.i("STAY", finalStayPoints.toString())

                                val sharedAudience  = _uiState.value.currentSessionParticipants

                                Log.i("SHARED", "audience: $sharedAudience")

                                // Remove user from active users
                                _locations.update {
                                    it - userId
                                }


                                _userPaths.update { current ->

                                    // Get user's path if it exists
                                    val existing = current[userId]
                                    val existingPath = current[userId]

                                    val safeStartedAt =
                                        existingPath?.startedAt
                                                .takeIf { it != 0L }
                                            ?: System.currentTimeMillis()

                                    // Compress the path to 2 points after the session ends
                                    // we only show start and end in timeline once the session ends
                                    // so we remove the middle points
                                    val compressed =
                                        if (existing != null && existing.points.size >= 2) {

                                            val start = existing.points.first()
                                            val end = existing.points.last()

                                            // Get address from latlng using Geocoder
                                            val startAddress = getAddressFromLatLng(
                                                start.latitude,
                                                start.longitude
                                            )
                                            val endAddress =
                                                getAddressFromLatLng(end.latitude, end.longitude)

                                            val userPathLatLng = existing.points.map {
                                                it.toUserPathLatLng()
                                            }

                                            if(startAddress != null && endAddress != null) {
                                                val sessionHistory = SessionHistory(
                                                    id = UUID.randomUUID().toString(),
                                                    userId = userId,
                                                    userName = it.alias ?: it.profileName,
                                                    startLat = start.latitude,
                                                    startLng = start.longitude,
                                                    endLat = end.latitude,
                                                    endLng = end.longitude,
                                                    startTime = safeStartedAt,
                                                    endTime = System.currentTimeMillis(),
                                                    startAddress = startAddress,
                                                    endAddress = endAddress,
                                                    userPath = userPathLatLng,
                                                    sharedWith = sharedAudience,
                                                    stayPoints = finalStayPoints
                                                )

                                                // Store session history in rtdb
                                                sessionHistoryRepository.saveSession(sessionHistory)
                                            }

                                            Log.i(
                                                "POLYLINE",
                                                "blocked, start: $startAddress, end: $endAddress"
                                            )

                                            UserPath(
                                                points = listOf(
                                                    start, end
                                                ),
                                                isActive = false
                                            )
                                        } else existing

                                    // Update user path with compressed points
                                    if (compressed != null)
                                        current + (userId to compressed)
                                    else
                                        current
                                }
                            }
                        }
                    }
            }
            // Store job in map to be able to cancel it later
            observerJobs[userId] = job
        }
    }
    private fun isValidLocation(lat: Double, lng: Double): Boolean {
        return lat in -90.0..90.0 &&
                lng in -180.0..180.0 &&
                !(lat == 0.0 && lng == 0.0)
    }


    // Get address from latlng using Geocoder
    private suspend fun getAddressFromLatLng(
        lat: Double,
        lng: Double
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(
                    context,
                    Locale.getDefault()
                )

                val address = geocoder.getFromLocation(lat, lng, 1)

                address?.firstOrNull()?.getAddressLine(0)
            } catch (e: Exception) {
                Log.e("GEOCODER", "Error: ${e.message}")
                null
            }
        }
    }

//    fun onUserLocationUpdate(
//        userId: String,
//        lat: Double,
//        lng: Double
//    ) {
//        val newPoint = LatLng(lat, lng)
//
//        _uiState.update { current ->
//
//            val existing = current.userPaths[userId]?.points.orEmpty()
//
//            if (existing.isNotEmpty()) {
//                val last = existing.last()
//                val distance = SphericalUtil
//                    .computeDistanceBetween(last, newPoint)
//
//                if (distance < 5) {
//                    return@update current
//                }
//            }
//
//            current.copy(
//                userPaths = current.userPaths + (userId to UserPath(
//                    points = existing + newPoint,
//                    isActive = true
//                ))
//            )
//        }
//    }
//
//    fun onSharingEnded(userId: String) {
//        _uiState.update { current ->
//
//            val existing = current.userPaths[userId] ?: return@update current
//
//            if (existing.points.size < 2) return@update current
//
//            val compressed = listOf(
//                existing.points.first(),
//                existing.points.last()
//            )
//
//            Log.i("POLYLINE", "start: ${compressed.first()} and last: ${compressed.last()}")
//
//            current.copy(
//                userPaths = current.userPaths + (userId to UserPath(
//                    points = compressed,
//                    isActive = false
//                ))
//            )
//        }
//    }


    /* Add this in order to reset paths for new session

    val existingPath = _uiState.value.userPaths[userId]

if (existingPath?.size == 2) {
    // Previous session summary exists → reset for new session
    _uiState.update {
        it.copy(
            userPaths = it.userPaths - userId
        )
    }
}
*/

    // job clean up when location sharing is not active
    fun cleanupRemovedUsers(activeUserIds: Set<String>) {
        observerJobs.keys
            .filter { it !in activeUserIds }
            .forEach { userId ->
                observerJobs.remove(userId)?.cancel()
                // Clean up stay tracker for removed user
                stayTrackers.remove(userId)?.reset()

                _locations.update {
                    it - userId
                }

                _liveStayPoints.update {
                    it - userId
                }
            }
    }


    /*
    No longer using room to store connections, instead using rtdb
     */
    fun addConnection(connection: GeoConnection) {
        viewModelScope.launch {
            connectionRepository.addNewConnection(connection)
        }
    }

    fun deleteConnection(connection: GeoConnection) {
        viewModelScope.launch {
            connectionRepository.deleteConnection(connection)
        }
    }

    // Remove this flow after full implementation session history
    val allConnections = connectionRepository.getConnections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allPlaces = placeRepository.getPlaces()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val alerts = geoActivityRepositoryImpl.observeAlerts(ActivityFilter.Today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Load all loved ones
    fun loadLovedOnes() {
        Log.i("Circle", "list: called")
        if (viewerId.isEmpty()) return

        viewModelScope.launch {


            when (val result =
                circleRepository.getAcceptedLovedOnes(viewerId)
            ) {
                is Resource.Success -> {
                    _uiState.update {

                        Log.i("Circle", "list: ${result.data}")
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
        viewModelScope.launch {
            combine(allConnections, allPlaces, alerts) { c, p, a ->
                Triple(c, p, a)
            }.collect { (connections, places, alerts) ->
                _uiState.update {
                    it.copy(
                        connectionsList = connections,
                        placesList = places,
                        alertsList = alerts
                    )
                }
            }
        }

        getUserProfile()
    }


    fun getUserProfile() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    username = googleSignInClient.getUserName(),
                    userAvatar = googleSignInClient.getUserProfile().toString()
                )
            }
        }
    }

    fun signOut() {
        observerJobs.values.forEach { it.cancel() }
        observerJobs.clear()

        // Clean up all stay trackers on sign-out
        stayTrackers.values.forEach { it.reset() }
        stayTrackers.clear()
        _liveStayPoints.value = emptyMap()

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                googleSignInClient.signOut()
            }
        }
    }


    // ── Stay Point Helper Methods ──

    // Called every time we get a location update for a user
    // This is the entry point - feeds the location into that user's tracker
    private fun feedStayTracker(userId: String, lat: Double, lng: Double, timestamp: Long) {

        // Get existing tracker for this user, or create a new one if first time
        val tracker = stayTrackers.getOrPut(userId) { StayPointTracker() }

        // Tell the tracker about this new location
        // The tracker will decide: is the user still at the same spot or did they move?
        tracker.onLocationUpdate(lat, lng, timestamp)

        // After updating the tracker, refresh what the UI sees
        emitLiveStayPoints(userId, tracker)
    }

    // Called when a user's session ends (Blocked state)
    // Returns the final list of all stay points to save in Firebase
    private fun finalizeStayTracker(userId: String): List<StayPoint> {

        // Remove the tracker from our map (session is over, we don't need it anymore)
        // If no tracker exists for this user, return empty list
        val tracker = stayTrackers.remove(userId) ?: return emptyList()

        // Tell the tracker the session is over
        // This capture the stay the user is currently in (if any)
        // and returns ALL stays (past ones + the current one)
        val stays = tracker.finalizeAll()

        // Remove this user's markers from the live map
        _liveStayPoints.update { it - userId }

        Log.i("STAY_POINT", "Finalized ${stays.size} stay points for user $userId")
        return stays
    }

    // Updates the _liveStayPoints flow so the map shows current stay markers
    // Called after every location update
    private fun emitLiveStayPoints(userId: String, tracker: StayPointTracker) {

        // Build the list of stay points to show on the map:
        val all = buildList {

            // 1. Add stays where the user already left
            addAll(tracker.finalizedStays)

            // 2. Add the stay the user is CURRENTLY at (if they've been there long enough)
            //    This is null if the user just arrived (under threshold)
            //    This is a StayPoint if they've been there >= threshold
            tracker.activeQualifiedStay?.let { add(it) }
        }

        // Update the flow that the UI observes
        if (all.isNotEmpty()) {
            // This user has stay points to show - add to map
            _liveStayPoints.update { it + (userId to all) }
        } else {
            // No stay points for this user - remove from map
            _liveStayPoints.update { it - userId }
        }
    }


}

data class HomeScreenUiState(
    val placesList: List<Place> = emptyList(),
    val connectionsList: List<GeoConnection> = emptyList(),
    val lovedOnes: List<CircleMember> = emptyList(),
    val currentViewers: List<User> = emptyList(),
    val currentSessionParticipants: List<String> = emptyList(),
    val alertsList: List<GeoAlert> = emptyList(),
    val userAvatar: String? = null,
    val username: String? = null,
    val viewerState: ViewerLocationState? = ViewerLocationState.Blocked,
    val remainingTime: String? = null
)