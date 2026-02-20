package com.aarav.geowav.data.model

data class TimelineItem(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val startLat: Double = 0.0,
    val startLng: Double = 0.0,
    val endLat: Double = 0.0,
    val endLng: Double = 0.0,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val startAddress: String = "",
    val endAddress: String = ""
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