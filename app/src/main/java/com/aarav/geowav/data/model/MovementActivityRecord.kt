package com.aarav.geowav.data.model

data class MovementActivityRecord(
    val placeName: String,
    val transition: ActivityTransition,
    val timestamp: Long,
    val dateKey: String,
    val readableTime: String,
    val latitude: Double,
    val longitude: Double
) {
    fun toPersonalHistoryPayload(): Map<String, Any> = mapOf(
        "geofenceId" to placeName,
        "placeName" to placeName,
        "transitionType" to when (transition) {
            ActivityTransition.ARRIVED -> "reached"
            ActivityTransition.LEFT -> "left"
        },
        "normalizedTransitionType" to transition.name,
        "timestamp" to timestamp,
        "dateKey" to dateKey,
        "readableTime" to readableTime,
        "location" to mapOf(
            "latitude" to latitude,
            "longitude" to longitude
        )
    )

    fun toFeedItem(actorId: String, actorName: String, actorAvatar: String?): CircleActivityItem =
        CircleActivityItem(
            actorId = actorId,
            actorName = actorName,
            actorAvatar = actorAvatar,
            placeName = placeName,
            normalizedTransitionType = transition.name,
            timestamp = timestamp
        )
}

data class CircleActivityItem(
    val actorId: String = "",
    val actorName: String = "",
    val actorAvatar: String? = null,
    val placeName: String = "",
    val normalizedTransitionType: String = "",
    val timestamp: Long = 0L
) {
    fun toFirebasePayload(): Map<String, Any> = buildMap {
        put("actorId", actorId)
        put("actorName", actorName)
        actorAvatar?.takeIf { it.isNotBlank() }?.let { put("actorAvatar", it) }
        put("placeName", placeName)
        put("normalizedTransitionType", normalizedTransitionType)
        put("timestamp", timestamp)
    }
}
