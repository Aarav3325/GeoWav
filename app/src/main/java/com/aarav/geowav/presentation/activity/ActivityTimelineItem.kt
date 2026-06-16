package com.aarav.geowav.presentation.activity

import com.aarav.geowav.data.model.CircleActivityItem

sealed class ActivityTimelineItem {
    abstract val id: String
    abstract val displayTimestamp: Long
    abstract val actorId: String
    abstract val actorName: String
    abstract val actorAvatar: String?
    abstract val placeName: String

    data class Event(
        val activity: CircleActivityItem,
        val story: ActivityStory
    ) : ActivityTimelineItem() {
        override val id: String = listOf(
            "event",
            activity.actorId,
            activity.placeName,
            activity.normalizedTransitionType,
            activity.timestamp
        ).joinToString("|")
        override val displayTimestamp: Long = activity.timestamp
        override val actorId: String = activity.actorId
        override val actorName: String = activity.actorName
        override val actorAvatar: String? = activity.actorAvatar
        override val placeName: String = activity.placeName
    }

    data class Visit(
        val arrival: CircleActivityItem,
        val departure: CircleActivityItem
    ) : ActivityTimelineItem() {
        override val id: String = listOf(
            "visit",
            arrival.actorId,
            arrival.placeName,
            arrival.timestamp,
            departure.timestamp
        ).joinToString("|")
        override val displayTimestamp: Long = departure.timestamp
        override val actorId: String = arrival.actorId
        override val actorName: String = arrival.actorName
        override val actorAvatar: String? = arrival.actorAvatar
        override val placeName: String = arrival.placeName
        val startedAt: Long = arrival.timestamp
        val endedAt: Long = departure.timestamp
        val durationMillis: Long = endedAt - startedAt
    }
}
