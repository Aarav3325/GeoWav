package com.aarav.geowav.core.insights

import com.aarav.geowav.data.mapper.FirebaseActivity
import com.aarav.geowav.data.model.ActivityTransition
import com.aarav.geowav.data.model.Place
import java.time.LocalDate
import java.time.ZoneId

data class CalculatedPlaceMetrics(
    val isEmptyState: Boolean,
    val relativeLastSeenText: String,
    val lastSeenValue: String,
    val visitsValue: String,
    val averageStayValue: String
)

object PlaceActivityMetricsCalculator {

    fun calculate(place: Place, activities: List<FirebaseActivity>): CalculatedPlaceMetrics {
        val matchingActivities = filterAndSortMatchingActivities(activities, place)

        if (matchingActivities.isEmpty()) {
            return CalculatedPlaceMetrics(
                isEmptyState = true,
                relativeLastSeenText = "Never visited yet",
                lastSeenValue = "--",
                visitsValue = "0",
                averageStayValue = "--"
            )
        }

        val lastVisitTimestamp = calculateLastVisit(matchingActivities)
        val (relativeText, shortText) = if (lastVisitTimestamp > 0L) {
            "Last sisited ${formatRelativeTime(lastVisitTimestamp)}" to formatGlanceableTime(lastVisitTimestamp)
        } else {
            "Never visited yet" to "--"
        }

        val visitsThisMonthCount = calculateVisitsThisMonth(matchingActivities)
        val avgStayMillis = calculateAverageStay(matchingActivities)
        val averageStayText = if (avgStayMillis > 0L) {
            formatStayDuration(avgStayMillis)
        } else {
            "--"
        }

        return CalculatedPlaceMetrics(
            isEmptyState = false,
            relativeLastSeenText = relativeText,
            lastSeenValue = shortText,
            visitsValue = visitsThisMonthCount.toString(),
            averageStayValue = averageStayText
        )
    }

    fun filterAndSortMatchingActivities(activities: List<FirebaseActivity>, place: Place): List<FirebaseActivity> {
        return activities.filter { activity ->
            val timestamp = activity.timestamp ?: 0L
            if (timestamp <= 0L) return@filter false

            val activityName = activity.placeName?.trim() ?: activity.geofenceId?.trim() ?: return@filter false
            val targetName = place.customName.ifBlank { place.placeName }.trim()

            activityName.equals(targetName, ignoreCase = true) ||
                    (place.customName.isNotBlank() && activityName.equals(place.customName.trim(), ignoreCase = true)) ||
                    (place.placeName.isNotBlank() && activityName.equals(place.placeName.trim(), ignoreCase = true))
        }.sortedBy { it.timestamp ?: 0L }
    }

    fun calculateLastVisit(matchingActivities: List<FirebaseActivity>): Long {
        return matchingActivities.lastOrNull()?.timestamp ?: 0L
    }

    fun calculateVisitsThisMonth(
        matchingActivities: List<FirebaseActivity>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Int {
        val today = LocalDate.now(zoneId)
        val startOfMonth = today.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfMonth = today.plusMonths(1).withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        return matchingActivities.count { activity ->
            val ts = activity.timestamp ?: 0L
            val transition = ActivityTransition.fromRaw(activity.normalizedTransitionType ?: activity.transitionType)
            transition == ActivityTransition.ARRIVED && ts >= startOfMonth && ts < endOfMonth
        }
    }

    fun calculateAverageStay(matchingActivities: List<FirebaseActivity>): Long {
        var openArrivalTimestamp: Long? = null
        val completedDurations = mutableListOf<Long>()
        for (activity in matchingActivities) {
            val ts = activity.timestamp ?: continue
            if (ts <= 0L) continue
            val transition = ActivityTransition.fromRaw(activity.normalizedTransitionType ?: activity.transitionType) ?: continue

            when (transition) {
                ActivityTransition.ARRIVED -> {
                    openArrivalTimestamp = ts
                }
                ActivityTransition.LEFT -> {
                    if (openArrivalTimestamp != null) {
                        val duration = ts - openArrivalTimestamp
                        if (duration > 0L) {
                            completedDurations.add(duration)
                        }
                    }
                    openArrivalTimestamp = null
                }
            }
        }

        return if (completedDurations.isNotEmpty()) {
            completedDurations.average().toLong()
        } else {
            0L
        }
    }

    private fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        if (diff < 0) return "just now"
        val minutes = diff / 60_000
        val hours = diff / 3_600_000
        val days = diff / 86_400_000

        return when {
            minutes < 1 -> "just now"
            minutes < 60 -> "$minutes min${if (minutes > 1) "s" else ""} ago"
            hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
            days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
            else -> {
                val sdf = java.text.SimpleDateFormat("dd MMM, h:mm a", java.util.Locale.getDefault())
                "on ${sdf.format(java.util.Date(timestamp))}"
            }
        }
    }

    private fun formatGlanceableTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        if (diff < 0) return "Now"
        val minutes = diff / 60_000
        val hours = diff / 3_600_000
        val days = diff / 86_400_000

        return when {
            minutes < 1 -> "Now"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            else -> "${days}d"
        }
    }

    private fun formatStayDuration(millis: Long): String {
        val totalMins = millis / 60_000
        val hours = totalMins / 60
        val mins = totalMins % 60
        return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    }
}
