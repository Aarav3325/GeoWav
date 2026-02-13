package com.aarav.geowav.presentation.circle

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.core.utils.encodeEmail
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.PendingInvite
import com.aarav.geowav.domain.repository.CircleRepository
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
    val firebaseAuth: FirebaseAuth,
    val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    private val currentUserId: String
        get() = firebaseAuth.currentUser?.uid.orEmpty()

    private val currentUserEmail: String
        get() = firebaseAuth.currentUser?.email.orEmpty()

    private val _uiState = MutableStateFlow(CircleUiState())
    val uiState: StateFlow<CircleUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CircleUiEvent>()
    val events = _events.asSharedFlow()

    private var hasInteractedWithName = false
    private var hasInteractedWithEmail = false

    fun updateName(name: String) {
        hasInteractedWithName = true
        _uiState.update {
            it.copy(
                name = name
            )
        }
        validateInput()
    }


    fun updateEmail(email: String) {
        hasInteractedWithName = true
        _uiState.update {
            it.copy(
                email = email
            )
        }
        validateInput()
    }

    fun validateInput() {
        val name = _uiState.value.name
        val email = _uiState.value.email

        val isNameValid = name.length >= 2
        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()

        _uiState.update {
            it.copy(
                isInputValid = isNameValid && isEmailValid,
                nameError = if (!isNameValid && hasInteractedWithName) "Name should be at least 2 characters" else null,
                emailError = if (!isEmailValid && hasInteractedWithEmail) "Invalid email" else null
            )
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

                        Log.i("Circle", "list: ${result.data}")
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

    fun loadPendingInvites() {
        if (currentUserId.isEmpty()) {
            emitError("User not authenticated")
            return
        }

        viewModelScope.launch {
            circleRepository.getPendingInvites(currentUserId).collect { pendingInvites ->
                _uiState.update {
                    it.copy(
                        pendingInvites = pendingInvites
                    )
                }
            }
        }
    }


    fun sendInvite(email: String, receiverName: String) {
        val trimmedEmail = encodeEmail(email)

        if (trimmedEmail.isEmpty()) {
            emitError("Email cannot be empty")
            return
        }

        if (receiverName.isEmpty()) {
            emitError("Name cannot be empty")
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

        val alreadyInvited = _uiState.value.pendingInvites.any {
            it.senderEmail == email
        }

        val isAlreadyLovedOne = _uiState.value.lovedOnes.any {
            it.receiverEmail == email
        }


        if (isAlreadyLovedOne) {
            emitError("User is already added to your circle")
            return
        }

        if (alreadyInvited) {
            emitError("You have already invited this user")
            return
        }


        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val currentUser = googleSignInClient.findUserByUserId(currentUserId)
            if (currentUser == null) {
                _uiState.update { it.copy(isLoading = false) }
                emitError("Unable to send invite")
                return@launch
            }

            val receiverUid = circleRepository.findUserByEmail(trimmedEmail)
            if (receiverUid == null) {
                _uiState.update { it.copy(isLoading = false) }
                emitError("User with email $trimmedEmail not found")
                return@launch
            }

            val alias = receiverName.trim().ifEmpty { null }

            when (
                val result = circleRepository.sendCircleInvite(
                    senderUid = currentUserId,
                    senderEmail = currentUser.email,
                    receiverEmail = email,
                    senderProfileName = currentUser.username,
                    receiverUid = receiverUid,
                    alias = alias ?: ""
                )
            ) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(CircleUiEvent.InviteSent)
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    emitError(result.message ?: "Failed to send invite")
                }

                else -> Unit
            }
        }
    }

    fun acceptInvite(
        senderUid: String
    ) {
        if (currentUserId.isEmpty()) {
            emitError("User not authenticated")
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(acceptingInviteId = senderUid)
            }

            val receiver = googleSignInClient.findUserByUserId(currentUserId)
            if (receiver == null) {
                _uiState.update { it.copy(isLoading = false) }
                emitError("Unable to accept invite")
                return@launch
            }

            val sender = googleSignInClient.findUserByUserId(senderUid)
            if (sender == null) {
                _uiState.update { it.copy(isLoading = false) }
                emitError("Unable to accept invite")
                return@launch
            }

            when (
                val result = circleRepository.acceptInvite(
                    receiverUid = currentUserId,
                    senderUid = senderUid,
                    senderProfileName = sender.username,
                    receiverProfileName = receiver.username
                )
            ) {
                is Resource.Success -> {
                    _uiState.update { it.copy(acceptingInviteId = null) }
                    loadLovedOnes()
                    loadPendingInvites()
                    _events.emit(CircleUiEvent.InviteAccepted)
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(acceptingInviteId = null) }
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
            _uiState.update { it.copy(rejectingInviteId = senderUid) }

            when (
                val result = circleRepository.rejectInvite(
                    receiverUid = currentUserId,
                    senderUid = senderUid
                )
            ) {
                is Resource.Success -> {
                    _uiState.update { it.copy(rejectingInviteId = null) }

                    loadPendingInvites()
                    _events.emit(
                        CircleUiEvent.ShowError("Invite rejected")
                    )
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(rejectingInviteId = null) }
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
    val lovedOnes: List<CircleMember> = emptyList(),
    val pendingInvites: List<PendingInvite> = emptyList(),
    val isLoading: Boolean = false,
    val acceptingInviteId: String? = null,
    val rejectingInviteId: String? = null,
    val name: String = "",
    val email: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val isInputValid: Boolean = false
)

sealed class CircleUiEvent {
    object InviteSent : CircleUiEvent()
    object InviteAccepted : CircleUiEvent()
    data class ShowError(val message: String) : CircleUiEvent()
}

