package com.aarav.geowav.presentation.locationsharing

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.LiveLocationState
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.core.utils.ServiceState
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.domain.repository.CircleRepository
import com.aarav.geowav.domain.repository.EmergencySharingRepository
import com.aarav.geowav.domain.repository.LiveLocationSharingRepository
import com.aarav.geowav.domain.repository.LocationPermissionRepository
import com.aarav.geowav.platform.LiveLocationService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationSharingVM
@Inject constructor(
    @ApplicationContext val context: Context,
    val sharedPreferences: SharedPreferences,
    val firebaseAuth: FirebaseAuth,
    val circleRepository: CircleRepository,
    val locationPermissionRepository: LocationPermissionRepository,
    val emergencySharingRepository: EmergencySharingRepository,
    val locationLocationSharingRepository: LiveLocationSharingRepository
) : ViewModel() {

//
//    init {
//        sharedPreferences.edit {
//            remove("live_location_state")
//        }
//    }


    val ACTION_STOP = "ACTION_STOP_LIVE_LOCATION"
    private var _uiState: MutableStateFlow<LiveLocationUiState> =
        MutableStateFlow(LiveLocationUiState(sharingState = LiveLocationState.NotSharing))
    val uiState: StateFlow<LiveLocationUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LiveLocationUiEvent>()
    val events = _events.asSharedFlow()

    private var timestampJob: Job? = null


    private val currentUserId: String
        get() = firebaseAuth.currentUser?.uid.orEmpty()


    init {
        val recovered = readServiceState()
        _uiState.update {
            it.copy(sharingState = recovered)
        }

        observeEmergency()
        recoverActualSharingState()

//        if(recovered !is LiveLocationState.NotSharing) {
//            getLatestTimestamp()
//        }
    }

    private var emergencyTimerJob: Job? = null

    // Observe emergency state from firebase
    private fun observeEmergency() {
        if (currentUserId.isEmpty()) return


        viewModelScope.launch {
            emergencySharingRepository.observeEmergency(currentUserId)
                .collect { info ->

                    // Cancel previous timer
                    emergencyTimerJob?.cancel()
                    val restoreState = _uiState.value.previousSharingState
                        ?: LiveLocationState.NotSharing

                    if (info == null) {
                        _uiState.update {
                            it.copy(
                                emergencyEndsAt = null,
                                sharingState = restoreState,
                            )
                        }
                        return@collect
                    }

                    emergencyTimerJob = viewModelScope.launch {
                        while (true) {
                            val remaining = formatRemaining(info.endsAt)

                            Log.i("EMERGENCY", remaining)

                            _uiState.update {
                                it.copy(
                                    emergencyEndsAt = info.endsAt,
                                    remaining = remaining,
                                    sharingState =
                                        LiveLocationState.EmergencySharing(remaining)
                                )
                            }

                            if (remaining == "00:00") {
                                stopEmergencyInternal() // auto-stop service on timeout
                                break
                            }
                            delay(1_000)
                        }
                    }
                }
        }
    }


    // start emergency mode
    fun startEmergency(duration: Int = 30) {
        if (currentUserId.isEmpty()) return

        viewModelScope.launch {

            // set prev state to sharing if user was already sharing location
            val wasSharingBefore =
                _uiState.value.sharingState is LiveLocationState.Sharing

            _uiState.update {
                it.copy(
                    previousSharingState = if (wasSharingBefore) it.sharingState else null,
                    isEmergencyLoading = true
                )
            }

            try {

                val endsAt = System.currentTimeMillis() + duration * 60_000

                val viewerIds = _uiState.value.lovedOnes.map { it.id }

                emergencySharingRepository.startEmergency(currentUserId, endsAt, viewerIds)


                val wasSharingBefore =
                    _uiState.value.previousSharingState is LiveLocationState.Sharing

                // only start service if user was not sharing location before
                if (!wasSharingBefore) {
                    val intent = Intent(context, LiveLocationService::class.java)
                    context.startForegroundService(intent)
                }

                // It should be updated by observe function or else i am done
//                _uiState.update {
//                    it.copy(
//                        emergencyEndsAt = endsAt,
//                        sharingState = LiveLocationState.EmergencySharing(
//                            remainingTime = "30:00"
//                        )
//                    )
//                }
            } catch (e: Exception) {
                emitError("Failed to start emergency sharing")
            } finally {
                _uiState.update {
                    it.copy(
                        isEmergencyLoading = false
                    )
                }
            }
        }
    }

    // stop emergency mode
    fun stopEmergency() {
        if (currentUserId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isEmergencyLoading = true,
                    sharingState = LiveLocationState.NotSharing
                )
            }

            try {

                emergencySharingRepository.stopEmergency(currentUserId)

                val wasSharingBefore =
                    _uiState.value.previousSharingState is LiveLocationState.Sharing

                // only stop service if user was not sharing location before
                if (!wasSharingBefore) {
                    val intent = Intent(context, LiveLocationService::class.java).apply {
                        action = ACTION_STOP
                    }
                    context.startService(intent)
//                    stopLiveLocationSharing()
                }

            } catch (e: Exception) {
                emitError("Failed to stop emergency sharing")
            } finally {
                _uiState.update {
                    it.copy(
                        isEmergencyLoading = false
                    )
                }
            }
        }
    }


    // auto-stop emergency mode on time-out
    fun stopEmergencyInternal() {

        if (currentUserId.isEmpty()) return


        val wasSharingBefore =
            _uiState.value.previousSharingState is LiveLocationState.Sharing

        viewModelScope.launch {
            try {
                emergencySharingRepository.stopEmergency(currentUserId)

                if (!wasSharingBefore) {
                    // Only stop service if emergency was the only reason it was running
                    val intent = Intent(context, LiveLocationService::class.java).apply {
                        action = ACTION_STOP
                    }
                    context.startService(intent)

//                    stopLiveLocationSharing()
                }

                val restoreState = _uiState.value.previousSharingState
                    ?: LiveLocationState.NotSharing

                _uiState.update {
                    it.copy(
                        emergencyEndsAt = null,
                        sharingState = restoreState,
                        previousSharingState = null
                    )
                }

                if (restoreState !is LiveLocationState.Sharing) {
                    stopTimestampListener()
                }
            } catch (e: Exception) {
                emitError("Failed to auto-stop emergency sharing")
            }
        }
    }


    // recover state using preferences in case of app crash or kill for safe recover
    private fun readServiceState(): LiveLocationState {
        return when (
            sharedPreferences.getString(
                "live_location_state",
                ServiceState.NOT_SHARING.name
            )
        ) {
            ServiceState.STARTING.name -> LiveLocationState.Starting

            ServiceState.SHARING.name ->
                LiveLocationState.Sharing(
                    visibleCount = 0,
                    lastUpdatedText = System.currentTimeMillis()
                )

//            ServiceState.ERROR.name ->
//                LiveLocationState.Error("Failed to share live location")

            else -> LiveLocationState.NotSharing
        }
    }

    // recover actual starting state from firebase in case or kill or crash
    private fun recoverActualSharingState() {
        if (currentUserId.isEmpty()) return

        viewModelScope.launch {
            val isActive = locationLocationSharingRepository
                .isLiveLocationActive(currentUserId)

            _uiState.update {
                if (isActive) {
                    it.copy(
                        sharingState = LiveLocationState.Sharing(
                            visibleCount = it.selectedViewerIds.size,
                            lastUpdatedText = System.currentTimeMillis()
                        )
                    )
                } else {
                    it.copy(
                        sharingState = LiveLocationState.NotSharing
                    )
                }
            }

            if (isActive) {
                getLatestTimestamp()
            } else {
                stopTimestampListener()
            }
        }
    }


//    fun readServiceState(): LiveLocationState {
//        return when (
//            sharedPreferences.getString("live_location_state", "NotSharing")
//        ) {
//            "SHARING" -> LiveLocationState.Sharing(
//                visibleCount = _uiState.value.selectedViewerIds.size,
//                lastUpdatedText = "Updating..."
//            )
//
//            "ERROR" -> LiveLocationState.Error("Failed to share live location")
//            "NotSharing" -> LiveLocationState.NotSharing
//            else -> LiveLocationState.NotSharing
//        }
//    }

    // start location sharing - normal mode
    fun startLiveLocationSharing() {
        val viewers = _uiState.value.selectedViewerIds

        if (viewers.isEmpty()) {
            emitError("Select at least one person to share location with")
            return
        }

        if (_uiState.value.emergencyEndsAt != null) {
            emitError("Cannot start sharing during emergency sharing")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        sharingState = LiveLocationState.Starting,
                        isServiceActionLoading = true
                    )
                }

                val existingViewers = locationPermissionRepository
                    .getAllowedViewers(currentUserId)
                    .first()

                val toAdd = viewers - existingViewers
                val toRemove = existingViewers - viewers

                // Add new viewers
                toAdd.forEach { viewerId ->
                    locationPermissionRepository.allowViewer(
                        currentUserId,
                        viewerId
                    )

                    Log.i("LOCATION_PERMISSION", "Allowing viewer: $viewerId")
                }

                // Remove old viewers
                toRemove.forEach { viewerId ->
                    locationPermissionRepository.revokeViewer(
                        currentUserId,
                        viewerId
                    )

                    Log.i("LOCATION_PERMISSION", "Revoking viewer: $viewerId")
                }

                // Update sharedWith list once
                locationPermissionRepository.updateSharedWith(
                    currentUserId,
                    viewers
                )

                // Start foreground service
                val intent = Intent(context, LiveLocationService::class.java)
                context.startForegroundService(intent)

                _uiState.update {
                    it.copy(
                        sharingState = LiveLocationState.Sharing(
                            visibleCount = viewers.size,
                            lastUpdatedText = System.currentTimeMillis()
                        )
                    )
                }

                getLatestTimestamp()

            } catch (e: Exception) {
                emitError("Failed to start sharing")
            } finally {
                _uiState.update { it.copy(isServiceActionLoading = false) }
            }
        }
    }


//    fun startLiveLocationSharing() {
//
//        if (_uiState.value.selectedViewerIds.isEmpty()) {
//            emitError("Select at least one person to share location with")
//            return
//        }
//
//        _uiState.update {
//            it.copy(
//                isServiceActionLoading  = true,
//            )
//        }
//
//        val intent = Intent(context, LiveLocationService::class.java)
//        context.startForegroundService(intent)
//    }

    // stop location sharing
    fun stopLiveLocationSharing() {

        stopTimestampListener()

        _uiState.update {
            it.copy(
                sharingState = LiveLocationState.NotSharing,
                isServiceActionLoading = true
            )
        }

        val intent = Intent(context, LiveLocationService::class.java).apply {
            action = ACTION_STOP
        }
        context.startService(intent)

        _uiState.value = _uiState.value.copy(
            showStoppedDialog = true,
            isServiceActionLoading = false
        )
    }

    fun refreshState() {
        _uiState.update {
            it.copy(
                sharingState = readServiceState(),
                isServiceActionLoading = false
            )
        }
    }

    // load viewer ids - who all can observe current user's location
    fun loadLocationPermission() {
        viewModelScope.launch {
            locationPermissionRepository.getAllowedViewers(currentUserId)
                .collect { viewers ->
                    _uiState.update {
                        it.copy(
                            selectedViewerIds = viewers,
                            sharingState = (it.sharingState as? LiveLocationState.Sharing)?.copy(
                                visibleCount = viewers.size
                            ) ?: it.sharingState
                        )
                    }
                }
        }
    }

    // Allow viewer to observe location based on switch
    fun onViewerToggle(viewerId: String, enabled: Boolean) {

        if (_uiState.value.emergencyEndsAt != null) {
            emitError("Cannot change during emergency sharing")
            return
        }

        _uiState.update { state ->
            val updated = if (enabled) {
                state.selectedViewerIds + viewerId
            } else {
                state.selectedViewerIds - viewerId
            }

            state.copy(selectedViewerIds = updated)
        }
    }


    // Load all loved ones
    fun loadLovedOnes() {

        if (currentUserId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isInitialLoading = true
                )
            }

            when (val result =
                circleRepository.getAcceptedLovedOnes(currentUserId)
            ) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            lovedOnes = result.data ?: emptyList(),
                            isInitialLoading = false
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false
                        )
                    }
                }

                else -> {}
            }
        }
    }

    // clear timestamp job
    private fun stopTimestampListener() {
        timestampJob?.cancel()
        timestampJob = null
    }


    // Get timestamp for last location update
    fun getLatestTimestamp() {

        if (timestampJob != null) return
        Log.i("TIMESTAMP", "Collector started")

        timestampJob = viewModelScope.launch {
            locationLocationSharingRepository
                .getUpdatedTimestamp(currentUserId)
                .collect { timestamp ->
                    Log.i("TIMESTAMP", "Collected: $timestamp")

                    _uiState.update { state ->
                        state.copy(
                            sharingState =
                                (state.sharingState as? LiveLocationState.Sharing)
                                    ?.copy(lastUpdatedText = timestamp)
                                    ?: state.sharingState
                        )
                    }
                }
        }
    }


    private fun emitError(message: String) {
        viewModelScope.launch {
            _events.emit(LiveLocationUiEvent.ShowError(message))
        }
    }

    private fun formatRemaining(endsAt: Long): String {
        val diffMs = endsAt - System.currentTimeMillis()
        if (diffMs <= 0) return "00:00"

        val totalSeconds = diffMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return "%02d:%02d".format(minutes, seconds)
    }


}

data class LiveLocationUiState(
    val sharingState: LiveLocationState,
    val previousSharingState: LiveLocationState? = null,
    val selectedViewerIds: Set<String> = emptySet(),
    val lovedOnes: List<CircleMember> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isServiceActionLoading: Boolean = false,
    val updatingViewerId: String? = null,
    val emergencyEndsAt: Long? = null,
    val remaining: String? = null,
    val lastUpdatedAt: String? = null,
    val isEmergencyLoading: Boolean = false,
    val showStoppedDialog: Boolean = false
)

sealed class LiveLocationUiEvent {
    //    object InviteSent : LiveLocationUiEvent()
//    object InviteAccepted : LiveLocationUiEvent()
    data class ShowError(val message: String) : LiveLocationUiEvent()
}