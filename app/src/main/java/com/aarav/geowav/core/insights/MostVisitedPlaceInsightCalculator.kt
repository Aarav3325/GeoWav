package com.aarav.geowav.core.insights

import com.aarav.geowav.data.mapper.FirebaseActivity
import com.aarav.geowav.data.model.ActivityTransition
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters


// Defines the period for which insights are calculated
enum class PersonalInsightScope {
    Week,
    Month
}

sealed interface Insights {
    
    data class MostVisitedPlaceInsight(
        val placeName: String,
        val visitCount: Int,
        val scope: PersonalInsightScope
    ): Insights

    data class AverageVisitDurationInsight(
        val placeName: String,
        val averageDurationMillis: Long,
        val sessionCount: Int,
        val scope: PersonalInsightScope
    ): Insights
}



fun mostVisitedPlaceInsight(
    activities: List<FirebaseActivity>,
    scope: PersonalInsightScope
): Insights.MostVisitedPlaceInsight? {
    val arrivalsByPlace = activities
        .asSequence()
        .mapNotNull { activity ->
            val timestamp = activity.timestamp?.takeIf { it > 0L } ?: return@mapNotNull null
            val placeName = activity.placeName?.trim()?.takeIf { it.isNotEmpty() }
                ?: activity.geofenceId?.trim()?.takeIf { it.isNotEmpty() }
                ?: return@mapNotNull null
            val transition = ActivityTransition.fromRaw(
                activity.normalizedTransitionType ?: activity.transitionType
            ) ?: return@mapNotNull null

            if (transition == ActivityTransition.ARRIVED) {
                placeName to timestamp
            } else {
                null
            }
        }
        .groupBy(keySelector = { it.first }, valueTransform = { it.second }) // Group by place

    return arrivalsByPlace
        .map { (placeName, timestamps) ->
            PlaceVisitCount(
                placeName = placeName,
                visitCount = timestamps.size
            )
        } // Convert into counts
        .sortedWith(
            compareByDescending<PlaceVisitCount> { it.visitCount }
                .thenBy { it.placeName.lowercase() }
                .thenBy { it.placeName }
        )
        .firstOrNull() // pick the highest
        ?.let { winner ->
            Insights.MostVisitedPlaceInsight(
                placeName = winner.placeName,
                visitCount = winner.visitCount,
                scope = scope
            )
        }
}


// Which place has the highest average stay duration
fun averageVisitDurationInsight(
    activities: List<FirebaseActivity>,
    scope: PersonalInsightScope
): Insights.AverageVisitDurationInsight? {
    val sessionsByPlace = activities
        .asSequence()
        .mapNotNull { activity ->
            val timestamp = activity.timestamp?.takeIf { it > 0L } ?: return@mapNotNull null
            val placeName = activity.placeName?.trim()?.takeIf { it.isNotEmpty() }
                ?: activity.geofenceId?.trim()?.takeIf { it.isNotEmpty() }
                ?: return@mapNotNull null
            val transition = ActivityTransition.fromRaw(
                activity.normalizedTransitionType ?: activity.transitionType
            ) ?: return@mapNotNull null

            TimedPlaceTransition(
                placeName = placeName,
                transition = transition,
                timestamp = timestamp
            )
        }
        .sortedBy { it.timestamp }
        .fold(mutableMapOf<String, PlaceVisitSessions>()) { sessionsByPlace, event ->
            val sessions = sessionsByPlace.getOrPut(event.placeName) {
                PlaceVisitSessions()
            }

            when (event.transition) {
                ActivityTransition.ARRIVED -> {
                    sessions.openArrivalTimestamp = event.timestamp
                }

                ActivityTransition.LEFT -> {
                    val arrivalTimestamp = sessions.openArrivalTimestamp
                    if (arrivalTimestamp != null) {
                        val duration = event.timestamp - arrivalTimestamp
                        if (duration > 0L) {
                            sessions.completedDurations += duration
                        }
                    }
                    sessions.openArrivalTimestamp = null
                }
            }

            sessionsByPlace
        }

    return sessionsByPlace
        .mapNotNull { (placeName, sessions) ->
            if (sessions.completedDurations.isEmpty()) return@mapNotNull null
            val averageDuration = sessions.completedDurations.sum() / sessions.completedDurations.size
            PlaceAverageDuration(
                placeName = placeName,
                averageDurationMillis = averageDuration,
                sessionCount = sessions.completedDurations.size
            )
        }
        .sortedWith(
            compareByDescending<PlaceAverageDuration> { it.averageDurationMillis }
                .thenBy { it.placeName.lowercase() }
                .thenBy { it.placeName }
        )
        .firstOrNull()
        ?.let { winner ->
            Insights.AverageVisitDurationInsight(
                placeName = winner.placeName,
                averageDurationMillis = winner.averageDurationMillis,
                sessionCount = winner.sessionCount,
                scope = scope
            )
        }
}

fun rangeForPersonalInsightScope(
    scope: PersonalInsightScope,
    today: LocalDate = LocalDate.now(INDIA_ZONE),
    zoneId: ZoneId = INDIA_ZONE
): Pair<Long, Long> {
    val startDate = when (scope) {
        PersonalInsightScope.Week ->
            today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        PersonalInsightScope.Month ->
            today.withDayOfMonth(1)
    }

    val startMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val endMillis = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

    return startMillis to endMillis
}

private data class PlaceVisitCount(
    val placeName: String,
    val visitCount: Int
)

private data class TimedPlaceTransition(
    val placeName: String,
    val transition: ActivityTransition,
    val timestamp: Long
)

private data class PlaceVisitSessions(
    var openArrivalTimestamp: Long? = null,
    val completedDurations: MutableList<Long> = mutableListOf()
)

private data class PlaceAverageDuration(
    val placeName: String,
    val averageDurationMillis: Long,
    val sessionCount: Int
)

private val INDIA_ZONE: ZoneId = ZoneId.of("Asia/Kolkata")
