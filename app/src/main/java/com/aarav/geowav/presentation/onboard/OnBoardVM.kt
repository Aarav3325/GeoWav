package com.aarav.geowav.presentation.onboard

import androidx.compose.foundation.pager.PagerState
import androidx.lifecycle.ViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class OnBoardVM @Inject constructor(): ViewModel() {

    private val _uiState = MutableStateFlow(OnBoardUIState())
    val uiState: StateFlow<OnBoardUIState> = _uiState.asStateFlow()

    fun onPageChanged(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
    }

    fun onContinueClicked() {
        if (_uiState.value.currentPage == _uiState.value.pages.lastIndex) {
            _uiState.update { it.copy(showPermissionDialog = true) }
        }
    }

    fun onPermissionDialogDismiss() {
        _uiState.update { it.copy(showPermissionDialog = false) }
    }

    fun onFineLocationResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                isFineLocationGranted = granted,
                requestingBackground = granted
            )
        }
    }

    fun onBackgroundLocationResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                isBackgroundGranted = granted,
                allPermissionsGranted = granted && it.isFineLocationGranted
            )
        }
    }
}


data class OnBoardUIState(
    val pages: List<OnBoardingPage> = OnBoardContent.pages,
    val currentPage: Int = 0,
    val showPermissionDialog: Boolean = false,
    val isFineLocationGranted: Boolean = false,
    val isBackgroundGranted: Boolean = false,
    val requestingBackground: Boolean = false,
    val allPermissionsGranted: Boolean = false
)
