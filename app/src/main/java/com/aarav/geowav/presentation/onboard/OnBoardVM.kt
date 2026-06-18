package com.aarav.geowav.presentation.onboard

import androidx.lifecycle.ViewModel
import com.aarav.geowav.core.permissions.GeoPermissionCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class OnBoardVM @Inject constructor(
    private val permissionCoordinator: GeoPermissionCoordinator
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnBoardUIState())
    val uiState: StateFlow<OnBoardUIState> = _uiState.asStateFlow()

    fun onPageChanged(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
    }

    fun onContinueClicked() {
        if (_uiState.value.currentPage == _uiState.value.pages.lastIndex) {
            _uiState.update { it.copy(showPermissionSetup = true) }
        }
    }

    fun onPermissionSetupDismiss() {
        _uiState.update { it.copy(showPermissionSetup = false) }
    }

    fun onFineLocationResult(granted: Boolean) {
        permissionCoordinator.refresh()
        _uiState.update {
            it.copy(
                isFineLocationGranted = granted,
                requestingBackground = granted
            )
        }
    }

    fun onBackgroundLocationResult(granted: Boolean) {
        permissionCoordinator.refresh()
        _uiState.update {
            it.copy(
                isBackgroundGranted = granted,
                allPermissionsGranted = granted && it.isFineLocationGranted
            )
        }
    }

    fun completeOnboarding(skippedPermissionSetup: Boolean) {
        permissionCoordinator.markOnboardingEducationComplete(skippedPermissionSetup)
        _uiState.update {
            it.copy(
                isOnboardingComplete = true,
                skippedPermissionSetup = skippedPermissionSetup,
                showPermissionSetup = false
            )
        }
    }
}


data class OnBoardUIState(
    val pages: List<OnBoardingPage> = OnBoardContent.pages,
    val currentPage: Int = 0,
    val showPermissionSetup: Boolean = false,
    val isFineLocationGranted: Boolean = false,
    val isBackgroundGranted: Boolean = false,
    val requestingBackground: Boolean = false,
    val allPermissionsGranted: Boolean = false,
    val isOnboardingComplete: Boolean = false,
    val skippedPermissionSetup: Boolean = false
)
