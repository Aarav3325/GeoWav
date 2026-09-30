package com.aarav.geowav.data.model

enum class SessionMode {
    NORMAL,
    JOURNEY,
    CHECK_IN,
    SAFETY,
    TEMPORARY_CIRCLE
}

enum class SessionStatus {
    ACTIVE,
    PAUSED,
    COMPLETED,
    EXPIRED,
    CANCELLED;

    fun isTerminal(): Boolean {
        return this == COMPLETED || this == EXPIRED || this == CANCELLED
    }
}

data class DestinationLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val name: String = "",
    val address: String = ""
)

data class SharingSession(
    val sessionId: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val mode: SessionMode = SessionMode.NORMAL,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val startedAt: Long = 0L,
    val expiresAt: Long? = null,
    val destinationPlaceId: String? = null,
    val destinationLocation: DestinationLocation? = null,
    val sharedWith: List<String> = emptyList(),
    val createdFrom: String? = null,
    val active: Boolean = true,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val timestamp: Long = 0L
)

fun SessionStatus.canTransitionTo(target: SessionStatus): Boolean {
    if (this == target) return true
    if (this.isTerminal()) return false
    return when (this) {
        SessionStatus.ACTIVE -> target == SessionStatus.PAUSED ||
                target == SessionStatus.COMPLETED ||
                target == SessionStatus.EXPIRED ||
                target == SessionStatus.CANCELLED
        SessionStatus.PAUSED -> target == SessionStatus.ACTIVE ||
                target == SessionStatus.COMPLETED ||
                target == SessionStatus.EXPIRED ||
                target == SessionStatus.CANCELLED
        else -> false
    }
}
