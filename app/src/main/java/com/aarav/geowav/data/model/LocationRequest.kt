package com.aarav.geowav.data.model

enum class LocationRequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    EXPIRED
}

data class LocationRequest(
    val requestId: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val recipientId: String = "",
    val status: LocationRequestStatus = LocationRequestStatus.PENDING,
    val requestedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (5 * 60 * 1000L),
    val approvedDurationMinutes: Int? = null
)
