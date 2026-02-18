package com.aarav.geowav.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.core.utils.ViewerLocationState
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.GeoAlert
import com.aarav.geowav.data.model.GeoConnection
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.model.User
import com.aarav.geowav.data.repository.GeoActivityRepositoryImpl
import com.aarav.geowav.data.repository.GeoConnectionRepositoryImpl
import com.aarav.geowav.data.repository.PlaceRepositoryImpl
import com.aarav.geowav.domain.repository.CircleRepository
import com.aarav.geowav.domain.repository.ViewerLocationRepository
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    private val connectionRepository: GeoConnectionRepositoryImpl,
    private val placeRepository: PlaceRepositoryImpl,
    private val geoActivityRepositoryImpl: GeoActivityRepositoryImpl,
    private val circleRepository: CircleRepository,
    private val viewerLocationRepository: ViewerLocationRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<HomeScreenUiState> =
        MutableStateFlow(HomeScreenUiState())
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    val viewerId = googleSignInClient.getUserId()

    private var countdownJob: Job? = null

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
                        _uiState.update { state ->

                            when (viewerState) {

                                is ViewerLocationState.NormalSharing -> {

                                    onUserLocationUpdate(
                                        userId = userId,
                                        lat = viewerState.location.lat,
                                        lng = viewerState.location.lng
                                    )

                                    state.copy(
                                        locations = state.locations + (userId to viewerState)
                                    )
                                }

                                is ViewerLocationState.EmergencySharing -> {
                                    onUserLocationUpdate(
                                        userId = userId,
                                        lat = viewerState.location.lat,
                                        lng = viewerState.location.lng
                                    )

                                    state.copy(
                                        locations = state.locations + (userId to viewerState)
                                    )
                                }

                                ViewerLocationState.Blocked -> {
                                    onSharingEnded(userId)

                                    state.copy(
                                        locations = state.locations - userId
                                    )
                                }
                            }
                        }
                    }

            }

            observerJobs[userId] = job
        }
    }

    fun onUserLocationUpdate(
        userId: String,
        lat: Double,
        lng: Double
    ) {
        val newPoint = LatLng(lat, lng)

        _uiState.update { current ->

            val existing = current.userPaths[userId].orEmpty()

            if (existing.isNotEmpty()) {
                val last = existing.last()
                val distance = SphericalUtil
                    .computeDistanceBetween(last, newPoint)

                if (distance < 5) {
                    return@update current
                }
            }

            current.copy(
                userPaths = current.userPaths + (userId to (existing + newPoint))
            )
        }
    }

    fun onSharingEnded(userId: String) {
        _uiState.update { current ->

            val existing = current.userPaths[userId] ?: return@update current

            if (existing.size < 2) return@update current

            val compressed = listOf(
                existing.first(),
                existing.last()
            )

            Log.i("POLYLINE", "start: ${compressed.first()} and last: ${compressed.last()}")

            current.copy(
                userPaths = current.userPaths + (userId to compressed)
            )
        }
    }


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

                _uiState.update {
                    it.copy(
                        locations = it.locations - userId
                    )
                }
            }
    }


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

            val activeViewerIds = _uiState.value.locations
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
    val locations: Map<String, ViewerLocationState> = emptyMap(),
    val userPaths: Map<String, List<LatLng>> = emptyMap(),
    val currentViewers: List<User> = emptyList(),
    val alertsList: List<GeoAlert> = emptyList(),
    val userAvatar: String? = null,
    val username: String? = null,
    val viewerState: ViewerLocationState? = ViewerLocationState.Blocked,
    val remainingTime: String? = null
)