package com.aarav.geowav.data.model


data class StayPoint(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val startedAt: Long = 0L,
    val endedAt: Long = 0L,
    val durationMillis: Long = 0L
) {
    companion object {
        const val STAY_RADIUS_METERS = 30.0

        const val STAY_MIN_DURATION_MS = 10L * 60L * 1000L // 10 minutes
    }
}
