package com.aarav.geowav.presentation.locationsharing

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.LiveLocationState
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.core.utils.ServiceState
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.domain.repository.CircleRepository
import com.aarav.geowav.domain.repository.EmergencySharingRepository
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LocationSharingVM
@Inject constructor(
    @ApplicationContext val context: Context,
    val sharedPreferences: SharedPreferences,
    val firebaseAuth: FirebaseAuth,
    val circleRepository: CircleRepository,
    val locationPermissionRepository: LocationPermissionRepository,
    val emergencySharingRepository: EmergencySharingRepository
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


    private val currentUserId: String
        get() = firebaseAuth.currentUser?.uid.orEmpty()


    init {
        val recovered = readServiceState()
        _uiState.update {
            it.copy(sharingState = recovered)
        }

        observeEmergency()
    }

    private var emergencyTimerJob: Job? = null

    private fun observeEmergency() {
        if (currentUserId.isEmpty()) return

        viewModelScope.launch {
            emergencySharingRepository.observeEmergency(currentUserId)
                .collect { info ->

                    // Cancel previous timer
                    emergencyTimerJob?.cancel()

                    if (info == null) {
                        _uiState.update {
                            it.copy(
                                emergencyEndsAt = null,
                                sharingState = readServiceState()
                            )
                        }
                        return@collect
                    }

                    emergencyTimerJob = viewModelScope.launch {
                        while (true) {
                            val remaining = formatRemaining(info.endsAt)

                            _uiState.update {
                                it.copy(
                                    emergencyEndsAt = info.endsAt,
                                    sharingState =
                                        LiveLocationState.EmergencySharing(remaining)
                                )
                            }

                            if (remaining == "00:00") break
                            delay(1_000)
                        }
                    }
                }
        }
    }


    fun startEmergency(duration: Int = 30) {
        if(currentUserId.isEmpty()) return

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isEmergencyLoading = true
                )
            }

            try {

                val endsAt = System.currentTimeMillis() + duration * 60_000

                val viewerIds = _uiState.value.lovedOnes.map { it.id }

                emergencySharingRepository.startEmergency(currentUserId, endsAt, viewerIds)

                val intent = Intent(context, LiveLocationService::class.java)
                context.startForegroundService(intent)

            // It should be updates by observeFunction or else i am done
//                _uiState.update {
//                    it.copy(
//                        emergencyEndsAt = endsAt,
//                        sharingState = LiveLocationState.EmergencySharing(
//                            remainingTime = "30:00"
//                        )
//                    )
//                }
            }
            catch (e: Exception) {
                emitError("Failed to start emergency sharing")
            }
            finally {
                _uiState.update {
                    it.copy(
                        isEmergencyLoading = false
                    )
                }
            }
        }
    }

    fun stopEmergency() {
        if(currentUserId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isEmergencyLoading = true,
                    sharingState = LiveLocationState.NotSharing
                )
            }

            try {

                emergencySharingRepository.stopEmergency(currentUserId)

                val intent = Intent(context, LiveLocationService::class.java).apply {
                    action = ACTION_STOP
                }
                context.startService(intent)
            }
            catch (e: Exception) {
                emitError("Failed to stop emergency sharing")
            }
            finally {
                _uiState.update {
                    it.copy(
                        isEmergencyLoading = false
                    )
                }
            }
        }
    }


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
                    lastUpdatedText = "Updating..."
                )

//            ServiceState.ERROR.name ->
//                LiveLocationState.Error("Failed to share live location")

            else -> LiveLocationState.NotSharing
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

    fun startSharing() {
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

                viewers.forEach { viewerId ->
                    locationPermissionRepository
                        .allowViewer(currentUserId, viewerId)
                }

                val intent = Intent(context, LiveLocationService::class.java)
                context.startForegroundService(intent)

                _uiState.update {
                    it.copy(
                        sharingState = LiveLocationState.Sharing(
                            visibleCount = viewers.size,
                            lastUpdatedText = "Just now"
                        )
                    )
                }

            } catch (e: IOException) {
                emitError("Failed to start sharing")
            } catch (e: Exception) {
                emitError("Failed to start sharing")
            } finally {
                _uiState.update { it.copy(isServiceActionLoading = false) }
            }
        }
    }


//    fun startSharing() {
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

    fun stopLiveLocationSharing() {

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

    private fun emitError(message: String) {
        viewModelScope.launch {
            _events.emit(LiveLocationUiEvent.ShowError(message))
        }
    }

    private fun formatRemaining(endsAt: Long): String {
        val diff = endsAt - System.currentTimeMillis()

        if (diff <= 0) return "00:00"

        val minutes = (diff / 1000) / 60
        val seconds = (diff / 1000) % 60

        return "%02d:%02d".format(minutes, seconds)
    }

}

data class LiveLocationUiState(
    val sharingState: LiveLocationState,
    val selectedViewerIds: Set<String> = emptySet(),
    val lovedOnes: List<CircleMember> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isServiceActionLoading: Boolean = false,
    val updatingViewerId: String? = null,
    val emergencyEndsAt: Long? = null,
    val isEmergencyLoading: Boolean = false,
    val showStoppedDialog: Boolean = false
)

sealed class LiveLocationUiEvent {
    //    object InviteSent : LiveLocationUiEvent()
//    object InviteAccepted : LiveLocationUiEvent()
    data class ShowError(val message: String) : LiveLocationUiEvent()
}