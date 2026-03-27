package com.aarav.geowav.core.utils

import android.icu.util.Calendar
import com.aarav.geowav.core.utils.ActivityFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val indiaZone: ZoneId = ZoneId.of("Asia/Kolkata")
private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(indiaZone)

fun Long.toLocalDateInIndia(): LocalDate =
    Instant.ofEpochMilli(this)
        .atZone(indiaZone)
        .toLocalDate()

fun LocalDate.startOfDayMillis(): Long = atStartOfDay(indiaZone).toInstant().toEpochMilli()

fun LocalDate.endOfDayMillis(): Long =
    this.plusDays(1).atStartOfDay(indiaZone).toInstant().toEpochMilli() - 1

data class TimeRange(val startMillis: Long, val endMillis: Long)

fun rangeForFilter(filter: ActivityFilter): TimeRange {
    val today = LocalDate.now(indiaZone)

    return when (filter) {
        ActivityFilter.Today -> {
            TimeRange(today.startOfDayMillis(), today.endOfDayMillis())
        }

        ActivityFilter.Yesterday -> {
            val yes = today.minusDays(1)
            TimeRange(yes.startOfDayMillis(), yes.endOfDayMillis())
        }

        ActivityFilter.Last7Days -> {
            val sevenDaysAgo = today.minusDays(6)
            TimeRange(sevenDaysAgo.startOfDayMillis(), today.endOfDayMillis())
        }

        is ActivityFilter.Between -> {
            TimeRange(filter.from.startOfDayMillis(), filter.to.endOfDayMillis())
        }
    }
}

fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hr ago"
        days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
        else -> {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
    }
}

fun formatRemainingForEmergency(endsAt: Long): String {
    val diffMs = endsAt - System.currentTimeMillis()
    if (diffMs <= 0) return "00:00"

    val totalSeconds = diffMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return "%02d:%02d".format(minutes, seconds)
}