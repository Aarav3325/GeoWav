package com.aarav.geowav.presentation.locationsharing

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.LiveLocationState
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.domain.repository.CircleRepository
import com.aarav.geowav.platform.LiveLocationService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationSharingVM
@Inject constructor(
    @ApplicationContext val context: Context,
    val sharedPreferences: SharedPreferences,
    val firebaseAuth: FirebaseAuth,
    val circleRepository: CircleRepository
) : ViewModel() {

    private var _uiState: MutableStateFlow<LiveLocationUiState> =
        MutableStateFlow(LiveLocationUiState(readServiceState()))
    val uiState: StateFlow<LiveLocationUiState> = _uiState.asStateFlow()

    private val currentUserId: String
        get() = firebaseAuth.currentUser?.uid.orEmpty()

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

    fun loadLovedOnes() {

        if (currentUserId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true
                )
            }

            when (val result =
                circleRepository.getAcceptedLovedOnes(currentUserId)
            ) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            lovedOnes = result.data ?: emptyList(),
                            isLoading = false
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                }

                else -> {}
            }
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
    val lovedOnes: List<CircleMember> = emptyList(),
    val isLoading: Boolean = false,
    val showStoppedDialog: Boolean = false
)
