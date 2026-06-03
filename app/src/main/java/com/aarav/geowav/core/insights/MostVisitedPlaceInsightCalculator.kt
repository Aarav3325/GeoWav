package com.aarav.geowav.core.insights

import com.aarav.geowav.data.mapper.FirebaseActivity
import com.aarav.geowav.data.model.ActivityTransition
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

enum class PersonalInsightScope {
    Week,
    Month
}

data class MostVisitedPlaceInsight(
    val placeName: String,
    val visitCount: Int,
    val scope: PersonalInsightScope
)

fun mostVisitedPlaceInsight(
    activities: List<FirebaseActivity>,
    scope: PersonalInsightScope
): MostVisitedPlaceInsight? {
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
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })

    return arrivalsByPlace
        .map { (placeName, timestamps) ->
            PlaceVisitCount(
                placeName = placeName,
                visitCount = timestamps.size
            )
        }
        .sortedWith(
            compareByDescending<PlaceVisitCount> { it.visitCount }
                .thenBy { it.placeName.lowercase() }
                .thenBy { it.placeName }
        )
        .firstOrNull()
        ?.let { winner ->
            MostVisitedPlaceInsight(
                placeName = winner.placeName,
                visitCount = winner.visitCount,
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

private val INDIA_ZONE: ZoneId = ZoneId.of("Asia/Kolkata")
