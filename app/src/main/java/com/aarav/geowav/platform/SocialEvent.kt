package com.aarav.geowav.platform

import com.aarav.geowav.data.model.GeoAlert

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

    data class Geofence(
        val geofence: GeoAlert
    ): SocialEvent()

    data class LocationRequestReceived(
        val requestId: String,
        val requesterId: String,
        val requesterName: String
    ): SocialEvent()

    data class LocationRequestResponded(
        val requestId: String,
        val accepted: Boolean,
        val respondentName: String
    ): SocialEvent()
}