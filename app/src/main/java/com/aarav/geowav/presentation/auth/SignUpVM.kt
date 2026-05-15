package com.aarav.geowav.presentation.auth

import android.app.Activity
import android.content.Context
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.data.authentication.AuthResult
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@HiltViewModel
class SignUpVM @Inject constructor(
    @ApplicationContext val context: Context,
    val googleSignInClient: GoogleSignInClient,
    val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<SignUpUIState> = MutableStateFlow(SignUpUIState())
    val uiState: StateFlow<SignUpUIState> = _uiState.asStateFlow()

    private val _events = Channel<SignUpEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var hasInteractedWithEmail: Boolean = false
    private var hasInteractedWithPassword: Boolean = false
    private var hasInteractedWithUsername: Boolean = false

    fun updateEmail(email: String) {
        hasInteractedWithEmail = true
        _uiState.update {
            it.copy(
                email = email
            )
        }
        validateInput()
    }

    fun updatePassword(password: String) {
        hasInteractedWithPassword = true
        _uiState.update {
            it.copy(
                password = password
            )
        }
        validateInput()
    }

    fun updateUsername(userName: String) {
        hasInteractedWithUsername = true
        _uiState.update {
            it.copy(
                username = userName
            )
        }
        validateInput()
    }

    fun signInWithGoogle(activity: Activity) {

        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                isLoading = true
            )
        }

        viewModelScope.launch {
            val result = googleSignInClient.signIn(activity)

            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is AuthResult.Success -> {
                    paymentRepository.syncEntitlements()
                    _events.send(SignUpEvent.NavigateToHome)
                }

                is AuthResult.Failure -> {
                    _events.send(SignUpEvent.ShowError(result.message))
                }
            }
        }
    }

    fun signUpWithEmailAndPassword(name: String, email: String, password: String) {
        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                isLoading = true
            )
        }

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                googleSignInClient.signUpUsingEmailAndPassword(name, email, password)
            }

            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is AuthResult.Success -> {
                    paymentRepository.syncEntitlements()
                    _events.send(SignUpEvent.NavigateToHome)
                }

                is AuthResult.Failure -> {
                    _events.send(SignUpEvent.ShowError(result.message))
                }
            }
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(
                error = null,
                showErrorDialog = false
            )
        }
    }

    fun showError(message: String) {
        _uiState.update {
            it.copy(
                error = message,
                showErrorDialog = true
            )
        }
    }

    fun showPassword(){
        _uiState.update {
            it.copy(
                isPasswordVisible = true
            )
        }
    }

    fun hidePassword(){
        _uiState.update {
            it.copy(
                isPasswordVisible = false
            )
        }
    }

    fun validateInput() {
        val email = _uiState.value.email
        val pass = _uiState.value.password
        val name = _uiState.value.username

        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isPasswordValid = pass.length >= 8
        val isUsernameValid = name.length > 2

        _uiState.update {
            it.copy(
                isInputValid = isEmailValid && isPasswordValid && isUsernameValid,
                usernameError = if (!isUsernameValid && hasInteractedWithUsername) "Enter valid username" else null,
                emailError = if (!isEmailValid && hasInteractedWithEmail) "Enter valid email address" else null,
                passwordError = if (!isPasswordValid && hasInteractedWithPassword) "Password must be at least 8 characters" else null
            )
        }
    }
}

data class SignUpUIState(
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isInputValid: Boolean = false,
    val error: String? = null,
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val showErrorDialog: Boolean = false
)

sealed interface SignUpEvent {
    data object NavigateToHome : SignUpEvent
    data class ShowError(val message: String) : SignUpEvent
}
