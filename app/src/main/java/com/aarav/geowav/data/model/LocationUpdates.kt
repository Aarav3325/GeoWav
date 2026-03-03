package com.aarav.geowav.data.model

import com.google.android.gms.maps.model.LatLng

data class LocationUpdates(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val startedAt: Long = 0L,
    val timestamp: Long = 0L,
    val sharedWith: List<String> = emptyList()
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

data class UserPathLatLng(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0L
)

fun UserPathLatLng.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

data class EmergencyInfo(
    val startedAt: Long = 0L,
    val endsAt: Long = 0L,
    val duration: Long = 0L,
    val viewers: Set<String> = emptySet()
)

data class UserPath(
    val startedAt: Long = 0L,
    val points: List<LatLng>,
    val isActive: Boolean
)

