package com.aarav.geowav.data.mapper

import com.aarav.geowav.data.model.GeoAlert

data class FirebaseLocation(
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class FirebaseActivity(
    val geofenceId: String? = null,
    val transitionType: String? = null,  // "ENTER" / "EXIT"
    val timestamp: Long? = null,
    val dateKey: String? = null,         // "yyyy-MM-dd"
    val readableTime: String? = null,    // "5:42 PM"
    val location: FirebaseLocation? = null
)

// Mappers.kt
fun FirebaseActivity.toGeoAlert(id: String): GeoAlert? {
    val geofenceId = geofenceId ?: return null
    val transition = transitionType ?: "UNKNOWN"
    val readable = readableTime ?: ""

    val ts = timestamp ?: return null
    val type = if (transition.equals("reached", ignoreCase = true)) "enter" else "exit"

    val zoneLabel = geofenceId

    val title = when (type) {
        "enter" -> "Reached $zoneLabel"
        else    -> "Left $zoneLabel"
    }

    val relativeTime = buildRelativeSubtitle(type, ts)


    return GeoAlert(
        id = id,
        title = title,
        subtitle = relativeTime,
        time = readable,
        readableTime = ts,
        type = type
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
        minutes < 1 -> "$verb just now"
        minutes < 60 -> "$verb $minutes min${if (minutes > 1) "s" else ""} ago"
        hours < 24 -> "$verb $hours hour${if (hours > 1) "s" else ""} ago"
        else -> {
            val df = java.text.SimpleDateFormat("dd MMM, h:mm a", java.util.Locale.getDefault())
            "$verb on ${df.format(java.util.Date(timestamp))}"
        }
    }
}
