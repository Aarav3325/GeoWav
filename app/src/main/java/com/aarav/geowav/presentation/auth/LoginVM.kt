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
class LoginVM @Inject constructor(
    val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    private val _uiState: MutableStateFlow<LoginUIState> = MutableStateFlow(LoginUIState())
    val uiState: StateFlow<LoginUIState> = _uiState.asStateFlow()

    private var hasInteractedWithEmail = false
    private var hasInteractedWithPassword = false

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

    fun signInWithEmailAndPassword(email: String, password: String) {
        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                isLoading = true
            )
        }

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                googleSignInClient.signInWithEmailAndPassword(email, password)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSignInSuccessful = result,
                    error = if (!result) "Invalid email or password" else null,
                    showErrorDialog = !result
                )
            }
        }

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
                    isSignInSuccessful = result,
                    error = if (!result) "Login failed due to an internal server error." else null,
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


    fun validateInput() {
        val email = _uiState.value.email
        val pass = _uiState.value.password

        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isPasswordValid = pass.length >= 8

        _uiState.update {
            it.copy(
                isInputValid = isEmailValid && isPasswordValid,
                emailError = if (!isEmailValid && hasInteractedWithEmail) "Enter valid email address" else null,
                passwordError = if (!isPasswordValid && hasInteractedWithPassword) "Password must be at least 8 characters" else null
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

}

data class LoginUIState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isInputValid: Boolean = false,
    val isSignInSuccessful: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val error: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val showErrorDialog: Boolean = false
)