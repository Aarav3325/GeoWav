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

    private var _uiState: MutableStateFlow<SharingUiState> =
        MutableStateFlow(SharingUiState(getLiveLocationState()))
    val uiState: StateFlow<SharingUiState> = _uiState.asStateFlow()

    fun getLiveLocationState(): LiveLocationState {
        return when (
            sharedPreferences.getString("live_location_state", null)
        ) {
            "Sharing" -> LiveLocationState.Sharing
            "NotSharing" -> LiveLocationState.NotSharing
            else -> LiveLocationState.NotSharing
        }
    }

    fun startLiveLocationSharing() {
        _uiState.update {
            it.copy(
                sharingState = LiveLocationState.Sharing
            )
        }

        val intent = Intent(context, LiveLocationService::class.java)
        context.startForegroundService(intent)
    }

    fun stopLiveLocationSharing() {
        val intent = Intent(context, LiveLocationService::class.java)
        context.stopService(intent)


        _uiState.value = _uiState.value.copy(
            sharingState = LiveLocationState.NotSharing,
            showStoppedDialog = true,
            isLoading = false
        )
    }
}

data class SharingUiState(
    val sharingState: LiveLocationState,
    val isLoading: Boolean = false,
    val showStoppedDialog: Boolean = false
)
