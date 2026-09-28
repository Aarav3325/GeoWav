package com.aarav.geowav.presentation.journey

import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.GeoNotificationHelper
import com.aarav.geowav.core.utils.NotificationType
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.DestinationLocation
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.model.SessionMode
import com.aarav.geowav.data.model.SessionStatus
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.data.repository.PlaceRepositoryImpl
import com.aarav.geowav.domain.repository.CircleRepository
import com.aarav.geowav.domain.repository.LiveLocationSharingRepository
import com.aarav.geowav.domain.repository.LocationPermissionRepository
import com.aarav.geowav.platform.LiveLocationService
import com.aarav.geowav.platform.LocationManager
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JourneyViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val placeRepository: PlaceRepositoryImpl,
    private val circleRepository: CircleRepository,
    private val liveLocationSharingRepository: LiveLocationSharingRepository,
    private val locationPermissionRepository: LocationPermissionRepository,
    private val googleSignInClient: GoogleSignInClient,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(JourneyUiState())
    val uiState: StateFlow<JourneyUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<JourneyUiEvent>()
    val events: SharedFlow<JourneyUiEvent> = _events.asSharedFlow()

    private var locationMonitorJob: Job? = null

    val currentUserId: String
        get() = googleSignInClient.getUserId()

    init {
        loadPlaces()
        loadCircleMembers()
        observeActiveJourney()
        observeUserLocation()
    }

    private fun observeUserLocation() {
        viewModelScope.launch {
            val last = locationManager.getLastKnownLocation()
            if (last != null) {
                _uiState.update { it.copy(userLocation = LatLng(last.latitude, last.longitude)) }
            }
            locationManager.getLocationUpdates().collect { loc ->
                _uiState.update { it.copy(userLocation = LatLng(loc.latitude, loc.longitude)) }
            }
        }
    }

    fun loadPlaces() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPlaces = true) }
            placeRepository.getPlaces().collect { places ->
                _uiState.update { state ->
                    state.copy(
                        savedPlaces = places,
                        isLoadingPlaces = false
                    )
                }
            }
        }
    }

    fun loadCircleMembers() {
        if (currentUserId.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMembers = true) }
            when (val result = circleRepository.getAcceptedLovedOnes(currentUserId)) {
                is Resource.Success -> {
                    val members = result.data ?: emptyList()
                    val defaultSelected = members.map { it.id }.toSet()
                    _uiState.update {
                        it.copy(
                            circleMembers = members,
                            selectedMemberIds = defaultSelected,
                            isLoadingMembers = false
                        )
                    }
                }
                else -> {
                    _uiState.update { it.copy(isLoadingMembers = false) }
                }
            }
        }
    }

    fun selectPlace(place: Place) {
        _uiState.update {
            it.copy(
                selectedPlace = place,
                customDestination = DestinationLocation(
                    latitude = place.latitude,
                    longitude = place.longitude,
                    name = place.customName.ifEmpty { place.placeName },
                    address = place.address ?: ""
                ),
                step = JourneyStep.MEMBER_SELECTION
            )
        }
    }

    fun selectCustomDestination(dest: DestinationLocation) {
        _uiState.update {
            it.copy(
                selectedPlace = null,
                customDestination = dest,
                step = JourneyStep.MEMBER_SELECTION
            )
        }
    }

    fun toggleMemberSelection(memberId: String) {
        _uiState.update { state ->
            val updated = if (state.selectedMemberIds.contains(memberId)) {
                state.selectedMemberIds - memberId
            } else {
                state.selectedMemberIds + memberId
            }
            state.copy(selectedMemberIds = updated)
        }
    }

    fun proceedToConfirmation() {
        if (_uiState.value.selectedMemberIds.isEmpty()) {
            emitError("Select at least one person to notify")
            return
        }
        _uiState.update { it.copy(step = JourneyStep.CONFIRMATION) }
    }

    fun backToStep(targetStep: JourneyStep) {
        _uiState.update { it.copy(step = targetStep) }
    }

    fun startJourney(userPlan: UserPlan = UserPlan.FREE) {
        val dest = _uiState.value.customDestination
        if (dest == null) {
            emitError("Please select a destination")
            return
        }

        val viewers = _uiState.value.selectedMemberIds
        if (viewers.isEmpty()) {
            emitError("Select at least one person to notify")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isActionLoading = true) }

                val existingViewers = locationPermissionRepository
                    .getAllowedViewers(currentUserId)
                    .first()

                val toAdd = viewers - existingViewers
                val toRemove = existingViewers - viewers

                toAdd.forEach { locationPermissionRepository.allowViewer(currentUserId, it) }
                toRemove.forEach { locationPermissionRepository.revokeViewer(currentUserId, it) }
                locationPermissionRepository.updateSharedWith(currentUserId, viewers.toSet())

                val intent = Intent(context, LiveLocationService::class.java).apply {
                    putExtra("USER_PLAN", userPlan.name)
                    putExtra("SESSION_MODE", SessionMode.JOURNEY.name)
                    putExtra("DESTINATION_LAT", dest.latitude)
                    putExtra("DESTINATION_LNG", dest.longitude)
                    putExtra("DESTINATION_NAME", dest.name)
                    putExtra("DESTINATION_ADDRESS", dest.address)
                    _uiState.value.selectedPlace?.let { putExtra("DESTINATION_PLACE_ID", it.placeId) }
                    putExtra("CREATED_FROM", "JOURNEY_MODE")
                }
                context.startForegroundService(intent)

                GeoNotificationHelper.show(
                    context = context,
                    channelId = "sharing_channel",
                    title = "Journey Started",
                    message = "You started a journey to ${dest.name}",
                    type = NotificationType.JourneyStarted
                )

                _uiState.update {
                    it.copy(
                        step = JourneyStep.ACTIVE_JOURNEY,
                        isActionLoading = false
                    )
                }

                _events.emit(JourneyUiEvent.JourneyStarted)
            } catch (e: Exception) {
                Log.e("JourneyVM", "Failed to start journey", e)
                emitError("Failed to start journey. Please try again.")
                _uiState.update { it.copy(isActionLoading = false) }
            }
        }
    }

    private fun observeActiveJourney() {
        if (currentUserId.isEmpty()) return

        viewModelScope.launch {
            liveLocationSharingRepository.observeActiveSession(currentUserId)
                .collect { session ->
                    _uiState.update { it.copy(activeSession = session) }
                    if (session != null && session.mode == SessionMode.JOURNEY && session.status == SessionStatus.ACTIVE) {
                        if (_uiState.value.step != JourneyStep.ACTIVE_JOURNEY && _uiState.value.step != JourneyStep.ARRIVED) {
                            _uiState.update { it.copy(step = JourneyStep.ACTIVE_JOURNEY) }
                        }
                        startLocationMonitoring(session.destinationLocation)
                    } else if (session == null || session.status.isTerminal()) {
                        stopLocationMonitoring()
                    }
                }
        }
    }

    private fun startLocationMonitoring(destination: DestinationLocation?) {
        if (destination == null || locationMonitorJob != null) return

        val destLatLng = LatLng(destination.latitude, destination.longitude)
        val radiusMeters = _uiState.value.selectedPlace?.radius?.toDouble() ?: 100.0

        locationMonitorJob = viewModelScope.launch {
            locationManager.getLocationUpdates().collect { location ->
                val currentLatLng = LatLng(location.latitude, location.longitude)
                val distance = SphericalUtil.computeDistanceBetween(currentLatLng, destLatLng)

                _uiState.update { it.copy(distanceToDestinationMeters = distance) }

                if (distance <= radiusMeters) {
                    onArrivalDetected(destination)
                }
            }
        }
    }

    private fun stopLocationMonitoring() {
        locationMonitorJob?.cancel()
        locationMonitorJob = null
    }

    private suspend fun onArrivalDetected(destination: DestinationLocation) {
        stopLocationMonitoring()
        try {
            liveLocationSharingRepository.stopSharingLiveLocation(currentUserId, SessionStatus.COMPLETED)

            GeoNotificationHelper.show(
                context = context,
                channelId = "geo_channel",
                title = "Arrival",
                message = "You arrived at ${destination.name}.",
                type = NotificationType.JourneyCompleted
            )

            _uiState.update {
                it.copy(
                    step = JourneyStep.ARRIVED,
                    distanceToDestinationMeters = 0.0
                )
            }
            _events.emit(JourneyUiEvent.JourneyCompleted)
        } catch (e: Exception) {
            Log.e("JourneyVM", "Failed during arrival completion", e)
        }
    }

    fun endJourneyManually() {
        stopLocationMonitoring()
        viewModelScope.launch {
            try {
                liveLocationSharingRepository.stopSharingLiveLocation(currentUserId, SessionStatus.COMPLETED)
                val intent = Intent(context, LiveLocationService::class.java).apply {
                    action = "ACTION_STOP_LIVE_LOCATION"
                    putExtra("STOP_STATUS", SessionStatus.COMPLETED.name)
                }
                context.startService(intent)

                _uiState.update {
                    it.copy(
                        step = JourneyStep.DESTINATION_SELECTION,
                        selectedPlace = null,
                        customDestination = null,
                        distanceToDestinationMeters = null
                    )
                }
                _events.emit(JourneyUiEvent.JourneyCancelled)
            } catch (e: Exception) {
                emitError("Failed to end journey")
            }
        }
    }

    fun continueJourney() {
        _uiState.update { it.copy(step = JourneyStep.ACTIVE_JOURNEY) }
    }

    fun changeDestination() {
        _uiState.update { it.copy(step = JourneyStep.DESTINATION_SELECTION) }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _events.emit(JourneyUiEvent.ShowError(message))
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationMonitoring()
    }
}
