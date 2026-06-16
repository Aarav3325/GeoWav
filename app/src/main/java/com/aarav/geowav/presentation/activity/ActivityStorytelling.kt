package com.aarav.geowav.presentation.activity

import com.aarav.geowav.core.utils.toLocalDateInIndia
import com.aarav.geowav.data.model.ActivityTransition
import com.aarav.geowav.data.model.CircleActivityItem

data class ActivityStory(
    val action: String,
    val suffix: String? = null,
    val reason: StoryReason = StoryReason.Original
)

enum class StoryReason {
    Original,
    BackAfterLeavingSamePlace,
    FirstActivityOfDay,
    PreviousArrivalAtPlace
}

fun deriveActivityStory(
    activity: CircleActivityItem,
    activities: List<CircleActivityItem>,
    hasMoreHistoryInFilter: Boolean
): ActivityStory {
    val transition = ActivityTransition.fromRaw(activity.normalizedTransitionType)
        ?: return ActivityStory(action = "was at")

    if (transition == ActivityTransition.LEFT) {
        return ActivityStory(action = "left")
    }

    val olderActorActivities = activities
        .filter { item ->
            item.actorId == activity.actorId &&
                    item.timestamp < activity.timestamp
        }
        .sortedByDescending { it.timestamp }

    val previousActorActivity = olderActorActivities.firstOrNull()
    if (
        previousActorActivity?.placeName == activity.placeName &&
        ActivityTransition.fromRaw(previousActorActivity.normalizedTransitionType) == ActivityTransition.LEFT
    ) {
        return ActivityStory(
            action = "is back at",
            reason = StoryReason.BackAfterLeavingSamePlace
        )
    }

    if (!hasMoreHistoryInFilter && olderActorActivities.none { item ->
            item.timestamp.toLocalDateInIndia() == activity.timestamp.toLocalDateInIndia()
        }
    ) {
        return ActivityStory(
            action = "started the day at",
            reason = StoryReason.FirstActivityOfDay
        )
    }

    val hasOlderArrivalAtPlace = olderActorActivities.any { item ->
        item.placeName == activity.placeName &&
                ActivityTransition.fromRaw(item.normalizedTransitionType) == ActivityTransition.ARRIVED
    }
    if (hasOlderArrivalAtPlace) {
        return ActivityStory(
            action = "returned to",
            reason = StoryReason.PreviousArrivalAtPlace
        )
    }

    return ActivityStory(action = "arrived at")
}
