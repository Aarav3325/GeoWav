package com.aarav.geowav.data.mapper

import com.aarav.geowav.data.model.GeoAlert
import com.aarav.geowav.data.model.ActivityTransition

data class FirebaseLocation(
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class FirebaseActivity(
    val geofenceId: String? = null,
    val placeName: String? = null,
    val transitionType: String? = null,
    val normalizedTransitionType: String? = null, // "ARRIVED" / "LEFT"
    val timestamp: Long? = null,
    val dateKey: String? = null,         // "yyyy-MM-dd"
    val readableTime: String? = null,    // "5:42 PM"
    val location: FirebaseLocation? = null,
    val userName: String? = null
)

// Mappers.kt
fun FirebaseActivity.toGeoAlert(id: String, username: String): GeoAlert? {
    val geofenceId = placeName?.takeIf { it.isNotBlank() }
        ?: geofenceId?.takeIf { it.isNotBlank() }
        ?: return null
    val readable = readableTime ?: ""


    val ts = timestamp ?: return null
    val transition = ActivityTransition.fromRaw(normalizedTransitionType ?: transitionType)
        ?: return null
    val type = when (transition) {
        ActivityTransition.ARRIVED -> "enter"
        ActivityTransition.LEFT -> "exit"
    }

    val zoneLabel = geofenceId

    val title = when (type) {
        "enter" -> "$username Reached $zoneLabel"
        else    -> "$username Left $zoneLabel"
    }

    val relativeTime = buildRelativeSubtitle(type, ts)


    return GeoAlert(
        id = id,
        title = title,
        subtitle = relativeTime,
        time = readable,
        readableTime = ts,
        type = type,
        timestamp = timestamp,
        userName = username
    )
}

private fun buildRelativeSubtitle(type: String, timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val minutes = diff / 60000
    val hours = diff / (60000 * 60)

    val verb = when (type) {
        "enter" -> "Reached"
        else    -> "Left"
    }

    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes min${if (minutes > 1) "s" else ""} ago"
        hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        else -> {
            val df = java.text.SimpleDateFormat("dd MMM, h:mm a", java.util.Locale.getDefault())
            "on ${df.format(java.util.Date(timestamp))}"
        }
    }
}
