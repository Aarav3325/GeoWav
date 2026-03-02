package com.aarav.geowav.data.model

/**
 * Represents a detected stay point where the user remained
 * within a [STAY_RADIUS_METERS] radius for at least [STAY_MIN_DURATION_MS].
 *
 * This is persisted inside [SessionHistory] and stored in Firebase
 * alongside the userPath for replay.
 */
data class StayPoint(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val startedAt: Long = 0L,
    val endedAt: Long = 0L,
    val durationMillis: Long = 0L
) {
    companion object {
        /** The radius within which the user is considered stationary. */
        const val STAY_RADIUS_METERS = 30.0

        /** Minimum time (ms) the user must stay within the radius to qualify. */
        const val STAY_MIN_DURATION_MS = 1L * 60L * 1000L // 10 minutes
    }
}
