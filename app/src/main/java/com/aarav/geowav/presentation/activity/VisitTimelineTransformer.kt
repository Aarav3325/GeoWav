package com.aarav.geowav.presentation.activity

import com.aarav.geowav.data.model.ActivityTransition
import com.aarav.geowav.data.model.CircleActivityItem

fun transformActivitiesToTimeline(
    activities: List<CircleActivityItem>,
    hasMoreHistoryInFilter: Boolean
): List<ActivityTimelineItem> {
    val chronologicalActivities = activities
        .filter { item -> item.timestamp > 0L }
        .sortedBy { item -> item.timestamp }

    val openArrivals = linkedMapOf<VisitKey, CircleActivityItem>()
    val timelineItems = mutableListOf<ActivityTimelineItem>()

    chronologicalActivities.forEach { activity ->
        val transition = ActivityTransition.fromRaw(activity.normalizedTransitionType)
        val key = activity.visitKeyOrNull()

        when {
            transition == ActivityTransition.ARRIVED && key != null -> {
                openArrivals.remove(key)?.let { unpairedArrival ->
                    timelineItems += unpairedArrival.toTimelineEvent(
                        activities = activities,
                        hasMoreHistoryInFilter = hasMoreHistoryInFilter
                    )
                }
                openArrivals[key] = activity
            }

            transition == ActivityTransition.LEFT && key != null -> {
                val arrival = openArrivals.remove(key)
                if (arrival != null && arrival.timestamp < activity.timestamp) {
                    timelineItems += ActivityTimelineItem.Visit(
                        arrival = arrival,
                        departure = activity
                    )
                } else {
                    timelineItems += activity.toTimelineEvent(
                        activities = activities,
                        hasMoreHistoryInFilter = hasMoreHistoryInFilter
                    )
                }
            }

            else -> {
                timelineItems += activity.toTimelineEvent(
                    activities = activities,
                    hasMoreHistoryInFilter = hasMoreHistoryInFilter
                )
            }
        }
    }

    openArrivals.values.forEach { unpairedArrival ->
        timelineItems += unpairedArrival.toTimelineEvent(
            activities = activities,
            hasMoreHistoryInFilter = hasMoreHistoryInFilter
        )
    }

    return timelineItems.sortedByDescending { item -> item.displayTimestamp }
}

private data class VisitKey(
    val actorId: String,
    val placeName: String
)

private fun CircleActivityItem.visitKeyOrNull(): VisitKey? {
    if (actorId.isBlank() || placeName.isBlank()) return null
    return VisitKey(actorId = actorId, placeName = placeName)
}

private fun CircleActivityItem.toTimelineEvent(
    activities: List<CircleActivityItem>,
    hasMoreHistoryInFilter: Boolean
): ActivityTimelineItem.Event {
    return ActivityTimelineItem.Event(
        activity = this,
        story = deriveActivityStory(
            activity = this,
            activities = activities,
            hasMoreHistoryInFilter = hasMoreHistoryInFilter
        )
    )
}
