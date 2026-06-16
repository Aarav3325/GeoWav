package com.aarav.geowav.presentation.activity

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun activityRelativeTime(timestamp: Long): String {
    val diffMillis = (System.currentTimeMillis() - timestamp).coerceAtLeast(0L)
    val minutes = diffMillis / 60_000L
    val hours = minutes / 60L
    val days = hours / 24L

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days == 1L -> "Yesterday"
        days < 7 -> "${days}d ago"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}

fun activityExactTime(timestamp: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
}

fun activityTimeRange(startedAt: Long, endedAt: Long): String {
    return "${activityExactTime(startedAt)} - ${activityExactTime(endedAt)}"
}

fun activityDuration(durationMillis: Long): String {
    val totalMinutes = (durationMillis / 60_000L).coerceAtLeast(0L)
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}
