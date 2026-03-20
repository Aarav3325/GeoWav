package com.aarav.geowav.presentation.home

import android.app.Activity
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.aarav.geowav.data.model.PaymentTransactions
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.model.SessionHistory
import com.aarav.geowav.data.model.StayPoint
import com.aarav.geowav.data.model.UpiApp
import com.aarav.geowav.data.model.User
import com.aarav.geowav.data.model.UserPath
import com.aarav.geowav.data.model.toUserPathLatLng
import com.aarav.geowav.data.repository.GeoActivityRepositoryImpl
import com.aarav.geowav.data.repository.GeoConnectionRepositoryImpl
import com.aarav.geowav.data.repository.PaymentRepositoryImpl
import com.aarav.geowav.data.repository.PlaceRepositoryImpl
import com.aarav.geowav.domain.repository.CircleRepository
import com.aarav.geowav.domain.repository.LiveLocationSharingRepository
import com.aarav.geowav.domain.repository.PaymentRepository
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
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<HomeScreenUiState> =
        MutableStateFlow(HomeScreenUiState())
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    private val _locations = MutableStateFlow<Map<String, ViewerLocationState>>(emptyMap())
    val locations = _locations.asStateFlow()

    private val _userPaths = MutableStateFlow<Map<String, UserPath>>(emptyMap())
    val userPaths = _userPaths.asStateFlow()

    private var pathJob: Job? = null


    private val _liveStayPoints = MutableStateFlow<Map<String, List<StayPoint>>>(emptyMap())
    val liveStayPoints: StateFlow<Map<String, List<StayPoint>>> = _liveStayPoints.asStateFlow()

//    val userSessionHistory = sessionHistoryRepository.getSessionsForUser("7sZTZoNLRpUBcJSevQJyNq2XRVw1")
//        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList<SessionHistory>())

    val viewerId = googleSignInClient.getUserId()

//    private var countdownJob: Job? = null

    private val observerJobs = mutableMapOf<String, Job>()


//    fun launchBillingFlow(
//        activity: Activity,
//        productId: String
//    ) {
//        viewModelScope.launch {
//            paymentRepository.processPurchases(activity, productId)
//        }
//    }

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

    fun loadLovedOnes() {
        Log.i("Circle", "list: called")
        if (viewerId.isEmpty()) return

        viewModelScope.launch {


            when (val result =
                circleRepository.getAcceptedLovedOnes(viewerId)
            ) {
                is Resource.Success -> {
                    _uiState.update {

                       // Log.i("Circle", "list: ${result.data}")
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

    fun drawAnimatedPath(
        path: List<LatLng>
    ) {

        if (path.size <= _uiState.value.playbackIndex + 1) return

        pathJob?.cancel()

        pathJob = viewModelScope.launch {
            for(i in _uiState.value.playbackIndex until path.size - 1) {

                val start = _uiState.value.lastPosition ?: path[i]
                val end = path[i + 1]

                val distance = SphericalUtil.computeDistanceBetween(start, end)
                val speed = _uiState.value.speed
                val duration = (distance / (35.0 * speed) * 1000).toLong()

                val steps = (duration / 16).toInt().coerceAtLeast(1)

                for(step in 0..steps) {
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
                    userAvatar = googleSignInClient.getUserProfile().toString()
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pathJob?.cancel()
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
    val userAvatar: String? = null,
    val username: String? = null,
    val viewerState: ViewerLocationState? = ViewerLocationState.Blocked,
    val remainingTime: String? = null
)

sealed class PurchaseUiEvent(

)