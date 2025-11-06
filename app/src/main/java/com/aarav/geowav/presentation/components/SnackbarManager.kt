package com.aarav.geowav.presentation.components

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState

object SnackbarManager {
    private var snackbarHostState: SnackbarHostState? = null

    fun bind(state: SnackbarHostState) {
        snackbarHostState = state
    }

    suspend fun showMessage(message: String) {
        if(snackbarHostState != null){
            Log.i("TEST", "triggered")
            snackbarHostState?.showSnackbar(message, duration = SnackbarDuration.Long)
        }
    }
}
