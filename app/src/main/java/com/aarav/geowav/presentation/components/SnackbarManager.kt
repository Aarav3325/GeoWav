package com.aarav.geowav.presentation.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState

object SnackbarManager {
    private var snackbarHostState: SnackbarHostState? = null

    fun bind(state: SnackbarHostState) {
        snackbarHostState = state
    }

    suspend fun showMessage(message: String) {
        snackbarHostState?.showSnackbar(message, duration = SnackbarDuration.Short)
    }
}
