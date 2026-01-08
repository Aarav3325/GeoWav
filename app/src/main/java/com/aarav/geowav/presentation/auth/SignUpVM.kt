package com.aarav.geowav.presentation.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.data.authentication.GoogleSignInClient
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@HiltViewModel
class SignUpVM @Inject constructor(
    val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    private val _uiState: MutableStateFlow<SignUpUIState> = MutableStateFlow(SignUpUIState())
    val uiState: StateFlow<SignUpUIState> = _uiState.asStateFlow()

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

    fun signInWithGoogle() {

        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                isLoading = true
            )
        }

        viewModelScope.launch {

            val result = withContext(Dispatchers.IO) {
                googleSignInClient.signIn()
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSignUpSuccessful = result,
                    error = if (!result) "Login failed due to an internal server error." else null,
                    showErrorDialog = !result
                )
            }
        }
    }

    fun signUpWithEmailAndPassword(name: String, email: String, password: String) {
        _uiState.update {
            it.copy(
                isLoading = true
            )
        }

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                googleSignInClient.signUpUsingEmailAndPassword(name, email, password)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSignUpSuccessful = result,
                    error = if (!result) "Invalid email or password" else null,
                    showErrorDialog = !result
                )
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
    val isSignUpSuccessful: Boolean = false,
    val error: String? = null,
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val showErrorDialog: Boolean = false
)