package com.aarav.geowav.core.utils

sealed class LiveLocationState {
    data object Idle: LiveLocationState()
    data object Sharing: LiveLocationState()
    data object NotSharing : LiveLocationState()
    data class Error(val message: String): LiveLocationState()
}