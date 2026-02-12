package com.aarav.geowav.presentation.locationsharing

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.aarav.geowav.core.utils.LiveLocationState
import com.aarav.geowav.platform.LiveLocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LocationSharingVM
@Inject constructor(
    @ApplicationContext val context: Context,
    val sharedPreferences: SharedPreferences
) : ViewModel() {

    private var _uiState: MutableStateFlow<LiveLocationUiState> =
        MutableStateFlow(LiveLocationUiState(readServiceState()))
    val uiState: StateFlow<LiveLocationUiState> = _uiState.asStateFlow()

    fun readServiceState(): LiveLocationState {
        return when (
            sharedPreferences.getString("live_location_state", "NOT_SHARING")
        ) {
            "SHARING" -> LiveLocationState.Sharing(
                visibleCount = 0,
                lastUpdatedText = "Updating..."
            )

            "ERROR" -> LiveLocationState.Error("Failed to share live location")
            "NotSharing" -> LiveLocationState.NotSharing
            else -> LiveLocationState.NotSharing
        }
    }

    fun startSharing() {
        _uiState.update {
            it.copy(
                isLoading = true,
            )
        }

        val intent = Intent(context, LiveLocationService::class.java)
        context.startForegroundService(intent)
    }

    fun stopLiveLocationSharing() {

        _uiState.update {
            it.copy(
                isLoading = true
            )
        }

        val intent = Intent(context, LiveLocationService::class.java)
        context.stopService(intent)

        _uiState.value = _uiState.value.copy(
            showStoppedDialog = true,
            isLoading = false
        )
    }

    fun refreshState() {
        _uiState.update {
            it.copy(
                sharingState = readServiceState(),
                isLoading = false
            )
        }
    }

}

data class LiveLocationUiState(
    val sharingState: LiveLocationState,
    val isLoading: Boolean = false,
    val showStoppedDialog: Boolean = false
)
