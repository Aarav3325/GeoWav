package com.aarav.geowav.core.insights

import com.aarav.geowav.data.mapper.FirebaseActivity
import com.aarav.geowav.data.model.ActivityTransition

private data class ValidActivity(
    val placeName: String,
    val transition: ActivityTransition,
    val timestamp: Long
)

fun weeklyAwarenessSummaryInsight(
    activities: List<FirebaseActivity>,
    scope: PersonalInsightScope
): Insights.WeeklyAwarenessSummaryInsight? {
    val validActivities = activities.mapNotNull { activity ->
        val timestamp = activity.timestamp?.takeIf { it > 0L } ?: return@mapNotNull null
        
        val placeName = activity.placeName?.trim()?.takeIf { it.isNotEmpty() }
            ?: activity.geofenceId?.trim()?.takeIf { it.isNotEmpty() }
            ?: return@mapNotNull null
            
        val transition = ActivityTransition.fromRaw(
            activity.normalizedTransitionType ?: activity.transitionType
        ) ?: return@mapNotNull null
        
        ValidActivity(placeName, transition, timestamp)
    }

    if (validActivities.isEmpty()) {
        return null
    }

    val arrivals = validActivities.count { it.transition == ActivityTransition.ARRIVED }
    val departures = validActivities.count { it.transition == ActivityTransition.LEFT }
    
    val placesVisited = validActivities
        .filter { it.transition == ActivityTransition.ARRIVED }
        .map { it.placeName }
        .distinct()
        .size

    val mostActivePlace = validActivities
        .groupBy { it.placeName }
        .map { (placeName, list) -> placeName to list.size }
        .sortedWith(
            compareByDescending<Pair<String, Int>> { it.second }
                .thenBy { it.first.lowercase() }
                .thenBy { it.first }
        )
        .firstOrNull()?.first

    return Insights.WeeklyAwarenessSummaryInsight(
        arrivals = arrivals,
        departures = departures,
        placesVisited = placesVisited,
        mostActivePlace = mostActivePlace,
        scope = scope
    )
}
