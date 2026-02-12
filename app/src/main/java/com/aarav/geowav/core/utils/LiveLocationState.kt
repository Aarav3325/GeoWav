package com.aarav.geowav.core.utils

sealed class LiveLocationState {

    object NotSharing : LiveLocationState()

    object Starting : LiveLocationState()

    data class Sharing(
        val visibleCount: Int,
        val lastUpdatedText: String
    ) : LiveLocationState()

    data class EmergencySharing(
        val remainingTime: String
    ) : LiveLocationState()

    data class Error(
        val message: String
    ) : LiveLocationState()
}
