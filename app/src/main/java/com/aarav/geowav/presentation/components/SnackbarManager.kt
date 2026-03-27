package com.aarav.geowav.presentation.components

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState

object SnackbarManager {
    private var snackbarHostState: SnackbarHostState? = null

    fun bind(state: SnackbarHostState) {
        snackbarHostState = state
        Log.i("SNACKBAR", "bind success")
    }

    suspend fun showMessage(message: String) {
        snackbarHostState?.showSnackbar(message, duration = SnackbarDuration.Short)
    }
}
