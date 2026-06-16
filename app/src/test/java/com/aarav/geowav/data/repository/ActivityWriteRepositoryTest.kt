package com.aarav.geowav.data.repository

import com.aarav.geowav.data.model.ActivityTransition
import com.aarav.geowav.data.model.MovementActivityRecord
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityWriteRepositoryTest {

    @Test
    fun suppressesDuplicateTransitionInsideDebounceWindow() {
        val latestState = LatestMovementActivityState(
            normalizedTransitionType = ActivityTransition.ARRIVED.name,
            timestamp = 1_000L
        )
        val activity = movementActivity(
            transition = ActivityTransition.ARRIVED,
            timestamp = 1_000L + MOVEMENT_DUPLICATE_DEBOUNCE_WINDOW_MS
        )

        assertTrue(shouldSuppressMovementActivity(activity, latestState))
    }

    @Test
    fun allowsOppositeTransitionInsideDebounceWindow() {
        val latestState = LatestMovementActivityState(
            normalizedTransitionType = ActivityTransition.ARRIVED.name,
            timestamp = 1_000L
        )
        val activity = movementActivity(
            transition = ActivityTransition.LEFT,
            timestamp = 2_000L
        )

        assertFalse(shouldSuppressMovementActivity(activity, latestState))
    }

    @Test
    fun allowsDuplicateTransitionAfterDebounceWindow() {
        val latestState = LatestMovementActivityState(
            normalizedTransitionType = ActivityTransition.LEFT.name,
            timestamp = 1_000L
        )
        val activity = movementActivity(
            transition = ActivityTransition.LEFT,
            timestamp = 1_001L + MOVEMENT_DUPLICATE_DEBOUNCE_WINDOW_MS
        )

        assertFalse(shouldSuppressMovementActivity(activity, latestState))
    }

    private fun movementActivity(
        transition: ActivityTransition,
        timestamp: Long
    ): MovementActivityRecord {
        return MovementActivityRecord(
            placeName = "Home",
            transition = transition,
            timestamp = timestamp,
            dateKey = "2026-06-16",
            readableTime = "9:00 AM",
            latitude = 0.0,
            longitude = 0.0
        )
    }
}
