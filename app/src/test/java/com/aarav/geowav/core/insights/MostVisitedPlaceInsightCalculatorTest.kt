package com.aarav.geowav.core.insights

import com.aarav.geowav.data.mapper.FirebaseActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class MostVisitedPlaceInsightCalculatorTest {

    @Test
    fun countsArrivalsOnly() {
        val insight = mostVisitedPlaceInsight(
            activities = listOf(
                activity("College", "ARRIVED", 1L),
                activity("College", "LEFT", 2L),
                activity("Gym", "ARRIVED", 3L)
            ),
            scope = PersonalInsightScope.Month
        )

        assertEquals("College", insight?.placeName)
        assertEquals(1, insight?.visitCount)
    }

    @Test
    fun ignoresMalformedRecords() {
        val insight = mostVisitedPlaceInsight(
            activities = listOf(
                activity("", "ARRIVED", 1L),
                activity("College", "UNKNOWN", 2L),
                activity("College", "ARRIVED", 0L),
                activity("College", "ARRIVED", 3L)
            ),
            scope = PersonalInsightScope.Week
        )

        assertEquals("College", insight?.placeName)
        assertEquals(1, insight?.visitCount)
    }

    @Test
    fun breaksTiesByPlaceNameDeterministically() {
        val insight = mostVisitedPlaceInsight(
            activities = listOf(
                activity("Gym", "ARRIVED", 1L),
                activity("College", "ARRIVED", 2L)
            ),
            scope = PersonalInsightScope.Month
        )

        assertEquals("College", insight?.placeName)
        assertEquals(1, insight?.visitCount)
    }

    @Test
    fun supportsOlderGeofenceIdRecords() {
        val insight = mostVisitedPlaceInsight(
            activities = listOf(
                FirebaseActivity(
                    geofenceId = "Library",
                    normalizedTransitionType = "ARRIVED",
                    timestamp = 1L
                )
            ),
            scope = PersonalInsightScope.Month
        )

        assertEquals("Library", insight?.placeName)
        assertEquals(1, insight?.visitCount)
    }

    @Test
    fun returnsNullWhenNoValidArrivalsExist() {
        val insight = mostVisitedPlaceInsight(
            activities = listOf(
                activity("College", "LEFT", 1L),
                activity("Gym", "UNKNOWN", 2L)
            ),
            scope = PersonalInsightScope.Week
        )

        assertNull(insight)
    }

    @Test
    fun weekScopeUsesCurrentCalendarWeek() {
        val zone = ZoneId.of("UTC")
        val (startMillis, endMillis) = rangeForPersonalInsightScope(
            scope = PersonalInsightScope.Week,
            today = LocalDate.of(2026, 6, 3),
            zoneId = zone
        )

        assertEquals(
            LocalDate.of(2026, 6, 1).atStartOfDay(zone).toInstant().toEpochMilli(),
            startMillis
        )
        assertEquals(
            LocalDate.of(2026, 6, 4).atStartOfDay(zone).toInstant().toEpochMilli() - 1,
            endMillis
        )
    }

    @Test
    fun monthScopeUsesCurrentCalendarMonth() {
        val zone = ZoneId.of("UTC")
        val (startMillis, endMillis) = rangeForPersonalInsightScope(
            scope = PersonalInsightScope.Month,
            today = LocalDate.of(2026, 6, 3),
            zoneId = zone
        )

        assertEquals(
            LocalDate.of(2026, 6, 1).atStartOfDay(zone).toInstant().toEpochMilli(),
            startMillis
        )
        assertEquals(
            LocalDate.of(2026, 6, 4).atStartOfDay(zone).toInstant().toEpochMilli() - 1,
            endMillis
        )
    }

    private fun activity(
        placeName: String,
        transition: String,
        timestamp: Long
    ): FirebaseActivity = FirebaseActivity(
        placeName = placeName,
        normalizedTransitionType = transition,
        timestamp = timestamp
    )
}
