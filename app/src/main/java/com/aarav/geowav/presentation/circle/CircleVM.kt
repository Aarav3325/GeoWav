package com.aarav.geowav.presentation.circle

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.FeatureAccess
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.core.utils.encodeEmail
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.PendingInvite
import com.aarav.geowav.data.model.UpgradeContext
import com.aarav.geowav.data.model.UpgradeReason
import com.aarav.geowav.data.model.UserPlan
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

    // Emit events to the UI
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

    // Validate input
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

    // Fetch loved ones
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

                        //Log.i("Circle", "list: ${result.data}")
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

    // Fetch pending invites
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

    // Send invite
    fun sendInvite(
        email: String,
        receiverName: String,
        userPlan: UserPlan
    ) {
        val trimmedEmail = encodeEmail(email)

        val max = FeatureAccess.maxConnections(userPlan)

        if (_uiState.value.lovedOnes.size >= max) {
            emitUpgrade(
                userPlan
            )
            return
        }

        if (trimmedEmail.isEmpty()) {
            emitError("Email cannot be empty")
            return
        }

        if (receiverName.isEmpty()) {
            emitError("Name cannot be empty")
            return
        }

        if (trimmedEmail == encodeEmail(currentUserEmail)) {
            emitError("You cannot invite yourself")
            return
        }

        if (currentUserId.isEmpty()) {
            emitError("User not authenticated")
            return
        }

        // Check if user is already invited
        val alreadyInvited = _uiState.value.pendingInvites.any {
            it.senderEmail == email
        }

        // Check if user is already a loved one
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
            _uiState.update { it.copy(sendingRequest = true) }

            val currentUser = googleSignInClient.findUserByUserId(currentUserId)
            if (currentUser == null) {
                _uiState.update { it.copy(sendingRequest = false) }
                emitError("Unable to send invite")
                return@launch
            }

            val receiverUid = circleRepository.findUserByEmail(trimmedEmail)
            if (receiverUid == null) {
                _uiState.update { it.copy(sendingRequest = false) }
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

                // Handle success and error
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            sendingRequest = false,
                            name = "",
                            nameError = null,
                            email = "",
                            emailError = null
                        )
                    }
                    _events.emit(CircleUiEvent.InviteSent)
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(sendingRequest = false) }
                    emitError(result.message ?: "Failed to send invite")
                }

                else -> Unit
            }
        }
    }

    // Accept invite
    fun acceptInvite(
        senderUid: String
    ) {
        if (currentUserId.isEmpty()) {
            emitError("User not authenticated")
            return
        }

        viewModelScope.launch {

            // Update state to disable button
            _uiState.update {
                it.copy(acceptingInviteId = senderUid)
            }

            val receiver = googleSignInClient.findUserByUserId(currentUserId)
            if (receiver == null) {
                _uiState.update { it.copy(acceptingInviteId = null) }
                emitError("Unable to accept invite")
                return@launch
            }

            val sender = googleSignInClient.findUserByUserId(senderUid)
            if (sender == null) {
                _uiState.update { it.copy(acceptingInviteId = null) }
                emitError("Unable to accept invite")
                return@launch
            }


            when (
                val result = circleRepository.acceptInvite(
                    receiverUid = currentUserId,
                    senderUid = senderUid,
                    senderEmail = sender.email,
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


    // Reject invite
    fun rejectInvite(senderUid: String) {
        if (currentUserId.isEmpty()) {
            emitError("User not authenticated")
            return
        }

        viewModelScope.launch {

            // Update state to disable button
            _uiState.update { it.copy(rejectingInviteId = senderUid) }

            when (
                val result = circleRepository.rejectInvite(
                    receiverUid = currentUserId,
                    senderUid = senderUid
                )
            ) {
                is Resource.Success -> {
                    _uiState.update { it.copy(rejectingInviteId = null) }

                    // Update UI
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

    // Remove member from circle
    fun deleteMember(circleMemberId: String) {
        if (currentUserId.isEmpty()) {
            emitError("User not authenticated")
            return
        }

        viewModelScope.launch {
            when (val result = circleRepository.deleteCircleMember(currentUserId, circleMemberId)) {
                is Resource.Success -> {
                    // Update UI
                    loadLovedOnes()
                    _events.emit(CircleUiEvent.MemberDeleted)
                }

                is Resource.Error -> {
                    emitError(result.message ?: "Failed to delete member")
                }

                else -> Unit
            }
        }
    }

    fun showDeleteDialog() {
        _uiState.update {
            it.copy(
                showDeleteDialog = true
            )
        }
    }

    fun hideDeleteDialog() {
        _uiState.update {
            it.copy(
                showDeleteDialog = false
            )
        }
    }

    fun emitUpgrade(plan: UserPlan) {
        viewModelScope.launch {

            val upgradeTo = FeatureAccess.nextPlan(plan) ?: return@launch

            _events.emit(
                CircleUiEvent.ShowUpgrade(
                    UpgradeContext(
                        upgradeTo = upgradeTo,
                        reason = UpgradeReason.MaxConnections
                    )
                )
            )
        }
    }

    // Emit error
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
    val sendingRequest: Boolean = false,
    val acceptingInviteId: String? = null,
    val rejectingInviteId: String? = null,
    val name: String = "",
    val email: String = "",
    val showDeleteDialog: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val isInputValid: Boolean = false
)

sealed class CircleUiEvent {
    object InviteSent : CircleUiEvent()
    object InviteAccepted : CircleUiEvent()

    object MemberDeleted : CircleUiEvent()
    data class ShowUpgrade(
        val context: UpgradeContext
    ) : CircleUiEvent()

    data class ShowError(val message: String) : CircleUiEvent()
}

