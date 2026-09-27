package com.aarav.geowav.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

data class SessionHistory(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val mode: SessionMode = SessionMode.NORMAL,
    val status: SessionStatus = SessionStatus.COMPLETED,
    val expiresAt: Long? = null,
    val destinationPlaceId: String? = null,
    val destinationLocation: DestinationLocation? = null,
    val createdFrom: String? = null,
    val startLat: Double = 0.0,
    val startLng: Double = 0.0,
    val endLat: Double = 0.0,
    val endLng: Double = 0.0,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val startAddress: String = "",
    val endAddress: String = "",
    val userPath: List<UserPathLatLng> = emptyList(),
    val sharedWith: List<String> = emptyList(),
    val stayPoints: List<StayPoint> = emptyList()
)

fun LatLng.toUserPathLatLng(): UserPathLatLng {
    return UserPathLatLng(
        latitude = latitude,
        longitude = longitude
    )
}
