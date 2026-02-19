package com.aarav.geowav.data.model

data class TimelineItem(
    val id: String,
    val userId: String,
    val name: String,
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val startTime: Long,
    val endTime: Long,
    val startAddress: String,
    val endAddress: String
)

fun SessionHistory.toTimelineItem(name: String): TimelineItem {
    return TimelineItem(
        id = id,
        userId = userId,
        name = name,
        startLat = startLat,
        startLng = startLng,
        endLat = endLat,
        endLng = endLng,
        startTime = startTime,
        endTime = endTime,
        startAddress = startAddress,
        endAddress = endAddress
    )


}