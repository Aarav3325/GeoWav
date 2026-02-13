package com.aarav.geowav.data.model

data class LocationUpdates(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val timestamp: Long = 0L,
)

data class LocationMeta(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val active: Boolean,
    val timestamp: Long = 0L,
    val startedAt: Long = 0L,
)

fun LocationUpdates.toMap(): Map<String, Any> {
    return mapOf(
        "lat" to lat,
        "lng" to lng,
        "timestamp" to timestamp,
    )
}

data class EmergencyInfo(
    val startedAt: Long = 0L,
    val endsAt: Long = 0L,
    val duration: Long = 0L,
)