package com.aarav.geowav.platform

sealed class SocialEvent {
    data class InviteReceived(
        val senderId: String,
        val senderName: String
    ): SocialEvent()

    data class InviteAccepted(
        val circleId: String,
        val userName: String
    ): SocialEvent()

    data class SharingStarted(
        val userId: String,
        val userName: String
    ): SocialEvent()

    data class SharingStopped(
        val userId: String,
        val userName: String
    ): SocialEvent()

    data class EmergencyStarted(
        val userId: String,
        val userName: String
    ): SocialEvent()

    data class EmergencyStopped(
        val userId: String,
        val userName: String
    ): SocialEvent()
}