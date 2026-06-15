package com.aarav.geowav.core.insights

import com.aarav.geowav.data.mapper.FirebaseActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeeklyAwarenessSummaryInsightCalculatorTest {

    @Test
    fun `test standard activity calculation`() {
        val activities = listOf(
            FirebaseActivity(
                placeName = "Work",
                transitionType = "ARRIVED",
                timestamp = 1000L
            ),
            FirebaseActivity(
                placeName = "Work",
                transitionType = "LEFT",
                timestamp = 2000L
            ),
            FirebaseActivity(
                placeName = "Home",
                transitionType = "ARRIVED",
                timestamp = 3000L
            ),
            FirebaseActivity(
                placeName = "Gym",
                transitionType = "ARRIVED",
                timestamp = 4000L
            ),
            FirebaseActivity(
                placeName = "Gym",
                transitionType = "LEFT",
                timestamp = 5000L
            )
        )

        val result = weeklyAwarenessSummaryInsight(activities, PersonalInsightScope.Week)

        // Expected:
        // arrivals: Work, Home, Gym = 3
        // departures: Work, Gym = 2
        // unique places visited (arrivals): Work, Home, Gym = 3
        // transition counts: Work = 2, Gym = 2, Home = 1
        // Work and Gym tie with 2 transitions. Alphabetically: "Gym" comes before "Work"
        // So most active place should be "Gym"
        
        assertEquals(3, result?.arrivals)
        assertEquals(2, result?.departures)
        assertEquals(3, result?.placesVisited)
        assertEquals("Gym", result?.mostActivePlace)
        assertEquals(PersonalInsightScope.Week, result?.scope)
    }

    @Test
    fun `test empty activities returns null`() {
        val activities = emptyList<FirebaseActivity>()
        val result = weeklyAwarenessSummaryInsight(activities, PersonalInsightScope.Week)
        assertNull(result)
    }

    @Test
    fun `test all malformed activities returns null`() {
        val activities = listOf(
            // Missing timestamp
            FirebaseActivity(placeName = "Work", transitionType = "ARRIVED"),
            // Negative timestamp
            FirebaseActivity(placeName = "Work", transitionType = "ARRIVED", timestamp = -5L),
            // Missing place name & geofenceId
            FirebaseActivity(transitionType = "ARRIVED", timestamp = 1000L),
            // Invalid transition type
            FirebaseActivity(placeName = "Work", transitionType = "STAYED", timestamp = 1000L)
        )
        val result = weeklyAwarenessSummaryInsight(activities, PersonalInsightScope.Week)
        assertNull(result)
    }

    @Test
    fun `test partially malformed activities still calculates correctly`() {
        val activities = listOf(
            FirebaseActivity(
                placeName = "Work",
                transitionType = "ARRIVED",
                timestamp = 1000L
            ),
            // Malformed
            FirebaseActivity(
                placeName = "",
                geofenceId = null,
                transitionType = "ARRIVED",
                timestamp = 1500L
            ),
            // Valid geofenceId fallback
            FirebaseActivity(
                geofenceId = "Geofence_Home",
                transitionType = "LEFT",
                timestamp = 2000L
            )
        )

        val result = weeklyAwarenessSummaryInsight(activities, PersonalInsightScope.Month)

        // Expected:
        // arrivals: 1 (Work)
        // departures: 1 (Geofence_Home)
        // unique places visited: 1 (Work)
        // most active place: tie between "Work" (1) and "Geofence_Home" (1) -> "Geofence_Home" comes first alphabetically
        
        assertEquals(1, result?.arrivals)
        assertEquals(1, result?.departures)
        assertEquals(1, result?.placesVisited)
        assertEquals("Geofence_Home", result?.mostActivePlace)
        assertEquals(PersonalInsightScope.Month, result?.scope)
    }

    @Test
    fun `test active place tie-breaker rules`() {
        val activities = listOf(
            // Place B: 1 transition
            FirebaseActivity(placeName = "PlaceB", transitionType = "ARRIVED", timestamp = 1000L),
            // placea: 1 transition
            FirebaseActivity(placeName = "placea", transitionType = "ARRIVED", timestamp = 2000L),
            // PlaceA: 1 transition
            FirebaseActivity(placeName = "PlaceA", transitionType = "ARRIVED", timestamp = 3000L)
        )

        val result = weeklyAwarenessSummaryInsight(activities, PersonalInsightScope.Week)

        // All have 1 transition.
        // lowercase order comparison: "placea" vs "placea" (case-insensitive "placea" vs "placea" vs "placeb")
        // "placea" and "PlaceA" lowercase is "placea", which is < "placeb" ("placeb").
        // Between "placea" and "PlaceA", they have identical lowercase names.
        // The secondary comparison is case-sensitive:
        // 'P' (ASCII 80) vs 'p' (ASCII 112).
        // Since 'P' < 'p', "PlaceA" should win.
        
        assertEquals("PlaceA", result?.mostActivePlace)
    }
}
