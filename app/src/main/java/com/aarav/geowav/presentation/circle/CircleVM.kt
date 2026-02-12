package com.aarav.geowav.presentation.circle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.domain.repository.CircleRepository
import com.aarav.geowav.presentation.locationsharing.LovedOneUi
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CircleVM
@Inject constructor(
    val circleRepository: CircleRepository,
    val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val currentUserId: String
        get() = firebaseAuth.currentUser?.uid.orEmpty()

    private val currentUserEmail: String
        get() = firebaseAuth.currentUser?.email.orEmpty()

    private val _uiState = MutableStateFlow(CircleUiState())
    val uiState: StateFlow<CircleUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CircleUiEvent>()
    val events = _events.asSharedFlow()


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
                    emitError(result.message ?: "Failed to load your loved ones")
                }

                else -> {}
            }
        }
    }


    fun sendInvite(email: String) {
        val trimmedEmail = email.trim().lowercase()

        if (trimmedEmail.isEmpty()) {
            emitError("Email cannot be empty")
            return
        }

        if (trimmedEmail == currentUserEmail.lowercase()) {
            emitError("You cannot invite yourself")
            return
        }

        if (currentUserId.isEmpty()) {
            emitError("User not authenticated")
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true
                )
            }


            val findResult = circleRepository.findUserByEmail(email)
            if (findResult == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false
                    )
                }
                emitError("User with email $trimmedEmail not found")
                return@launch
            } else {
                when (
                    val result = circleRepository.sendCircleInvite(
                        currentUserId,
                        currentUserEmail,
                        findResult
                    )
                ) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false
                            )
                        }

                        _events.emit(CircleUiEvent.InviteSent)
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false
                            )
                        }

                        emitError(result.message ?: "Failed to send invite")
                    }

                    else -> {}
                }
            }
        }
    }

    fun acceptInvite(
        senderUid: String,
        senderName: String,
        receiverName: String
    ) {
        if (currentUserId.isEmpty()) {
            emitError("User not authenticated")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (
                val result = circleRepository.acceptInvite(
                    receiverUid = currentUserId,
                    senderUid = senderUid,
                    senderName = senderName,
                    receiverName = receiverName
                )
            ) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false) }

                    // refresh loved ones so UI updates
                    loadLovedOnes()

                    _events.emit(
                        CircleUiEvent.ShowError("Invite accepted")
                    )
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    emitError(result.message ?: "Failed to accept invite")
                }

                else -> Unit
            }
        }
    }

    fun rejectInvite(senderUid: String) {
        if (currentUserId.isEmpty()) {
            emitError("User not authenticated")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (
                val result = circleRepository.rejectInvite(
                    receiverUid = currentUserId,
                    senderUid = senderUid
                )
            ) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false) }

                    _events.emit(
                        CircleUiEvent.ShowError("Invite rejected")
                    )
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    emitError(result.message ?: "Failed to reject invite")
                }

                else -> Unit
            }
        }
    }


    private fun emitError(message: String) {
        viewModelScope.launch {
            _events.emit(CircleUiEvent.ShowError(message))
        }
    }
}

data class CircleUiState(
    val lovedOnes: List<LovedOneUi> = emptyList(),
    val isLoading: Boolean = false
)

sealed class CircleUiEvent {
    object InviteSent : CircleUiEvent()
    data class ShowError(val message: String) : CircleUiEvent()
}

