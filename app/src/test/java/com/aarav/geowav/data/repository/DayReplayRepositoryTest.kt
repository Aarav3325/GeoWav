package com.aarav.geowav.data.repository

import com.aarav.geowav.data.mapper.FirebaseActivity
import com.aarav.geowav.data.model.ActivityTransition
import com.aarav.geowav.data.model.DayReplayTimeSection
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.domain.repository.GeoActivityRepository
import com.aarav.geowav.domain.repository.PlaceRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DayReplayRepositoryTest {

    private val geoActivityRepository = object : GeoActivityRepository {
        override fun observeAlerts(filter: com.aarav.geowav.core.utils.ActivityFilter) = kotlinx.coroutines.flow.emptyFlow<List<com.aarav.geowav.data.model.GeoAlert>>()
        override fun observeMostVisitedPlace(scope: com.aarav.geowav.core.insights.PersonalInsightScope) = kotlinx.coroutines.flow.emptyFlow<com.aarav.geowav.core.insights.Insights.MostVisitedPlaceInsight?>()
        override fun observeAverageVisitDuration(scope: com.aarav.geowav.core.insights.PersonalInsightScope) = kotlinx.coroutines.flow.emptyFlow<com.aarav.geowav.core.insights.Insights.AverageVisitDurationInsight?>()
        override fun observeWeeklyAwarenessSummary(scope: com.aarav.geowav.core.insights.PersonalInsightScope) = kotlinx.coroutines.flow.emptyFlow<com.aarav.geowav.core.insights.Insights.WeeklyAwarenessSummaryInsight?>()
        override fun observeActivityHistory() = kotlinx.coroutines.flow.emptyFlow<List<FirebaseActivity>>()
    }

    private val placeRepository = object : PlaceRepository {
        override suspend fun addPlace(place: Place) {}
        override fun getPlaces() = kotlinx.coroutines.flow.emptyFlow<List<Place>>()
        override suspend fun deletePlace(place: Place) {}
        override suspend fun updatePlace(place: Place) {}
        override suspend fun fetchPlace(placeId: String) = com.aarav.geowav.core.utils.Resource.Error<com.google.android.libraries.places.api.model.Place>("Not implemented")
        override suspend fun searchPlaces(query: String) = com.aarav.geowav.core.utils.Resource.Error<List<com.google.android.libraries.places.api.model.AutocompletePrediction>>("Not implemented")
        override suspend fun getPlaceById(placeId: String): Place? = null
    }

    private val repository = DayReplayRepositoryImpl(geoActivityRepository, placeRepository)

    private val indiaZone = ZoneId.of("Asia/Kolkata")

    private val places = listOf(
        Place(placeId = "place_home", customName = "Home", placeName = "Home", address = "123 Home St", latitude = 12.0, longitude = 77.0),
        Place(placeId = "place_office", customName = "Office", placeName = "Office", address = "Tech Park Office", latitude = 12.1, longitude = 77.1),
        Place(placeId = "place_starbucks", customName = "Starbucks", placeName = "Starbucks", address = "Starbucks Coffee", latitude = 12.2, longitude = 77.2)
    )

    @Test
    fun `test matching activities to saved places`() {
        val activities = listOf(
            FirebaseActivity(geofenceId = "Home", transitionType = "ARRIVED", timestamp = 1718500000000L),
            FirebaseActivity(geofenceId = "Office", transitionType = "ARRIVED", timestamp = 1718508000000L),
            FirebaseActivity(geofenceId = "place_starbucks", transitionType = "ARRIVED", timestamp = 1718512000000L),
            FirebaseActivity(geofenceId = "Unknown Place", transitionType = "ARRIVED", timestamp = 1718520000000L)
        )

        val replays = repository.buildDayReplays(activities, places)
        val allStops = replays.values.flatMap { it.stops }
        
        assertEquals(3, allStops.size)
        assertTrue(allStops.any { it.placeId == "place_home" })
        assertTrue(allStops.any { it.placeId == "place_office" })
        assertTrue(allStops.any { it.placeId == "place_starbucks" })
        assertTrue(allStops.none { it.placeName == "Unknown Place" })
    }

    @Test
    fun `test pairing arrived and left events`() {
        val activities = listOf(
            FirebaseActivity(geofenceId = "place_home", transitionType = "ARRIVED", timestamp = createTimestamp(7, 0)),
            FirebaseActivity(geofenceId = "place_home", transitionType = "LEFT", timestamp = createTimestamp(8, 30)),
            FirebaseActivity(geofenceId = "place_office", transitionType = "ARRIVED", timestamp = createTimestamp(9, 0)),
            FirebaseActivity(geofenceId = "place_office", transitionType = "LEFT", timestamp = createTimestamp(17, 30))
        )

        val replays = repository.buildDayReplays(activities, places)
        val testDate = Instant.ofEpochMilli(createTimestamp(7, 0)).atZone(indiaZone).toLocalDate()
        val dayReplay = replays[testDate]

        assertNotNull(dayReplay)
        assertEquals(2, dayReplay!!.stops.size)

        val firstStop = dayReplay.stops[0]
        assertEquals("place_home", firstStop.placeId)
        assertEquals(createTimestamp(7, 0), firstStop.arrivedAt)
        assertEquals(createTimestamp(8, 30), firstStop.departedAt)
        assertEquals(90 * 60 * 1000L, firstStop.stayDurationMillis)

        val secondStop = dayReplay.stops[1]
        assertEquals("place_office", secondStop.placeId)
        assertEquals(createTimestamp(9, 0), secondStop.arrivedAt)
        assertEquals(createTimestamp(17, 30), secondStop.departedAt)
        assertEquals(8.5 * 60 * 60 * 1000L, secondStop.stayDurationMillis?.toDouble() ?: 0.0, 0.1)
    }

    @Test
    fun `test ignore standalone left events`() {
        val activities = listOf(
            FirebaseActivity(geofenceId = "place_home", transitionType = "LEFT", timestamp = createTimestamp(8, 0)),
            FirebaseActivity(geofenceId = "place_office", transitionType = "ARRIVED", timestamp = createTimestamp(9, 0)),
            FirebaseActivity(geofenceId = "place_office", transitionType = "LEFT", timestamp = createTimestamp(17, 0))
        )

        val replays = repository.buildDayReplays(activities, places)
        val testDate = Instant.ofEpochMilli(createTimestamp(9, 0)).atZone(indiaZone).toLocalDate()
        val dayReplay = replays[testDate]

        assertNotNull(dayReplay)
        assertEquals(1, dayReplay!!.stops.size)
        assertEquals("place_office", dayReplay.stops[0].placeId)
    }

    @Test
    fun `test ongoing visit is marked currently here`() {
        val activities = listOf(
            FirebaseActivity(geofenceId = "place_home", transitionType = "ARRIVED", timestamp = createTimestamp(7, 0)),
            FirebaseActivity(geofenceId = "place_home", transitionType = "LEFT", timestamp = createTimestamp(8, 30)),
            FirebaseActivity(geofenceId = "place_office", transitionType = "ARRIVED", timestamp = createTimestamp(9, 0))
        )

        val replays = repository.buildDayReplays(activities, places)
        val testDate = Instant.ofEpochMilli(createTimestamp(7, 0)).atZone(indiaZone).toLocalDate()
        val dayReplay = replays[testDate]

        assertNotNull(dayReplay)
        assertEquals(2, dayReplay!!.stops.size)

        val ongoingStop = dayReplay.stops[1]
        assertEquals("place_office", ongoingStop.placeId)
        assertEquals(createTimestamp(9, 0), ongoingStop.arrivedAt)
        assertNull(ongoingStop.departedAt)
        assertNull(ongoingStop.stayDurationMillis)
    }

    @Test
    fun `test adjustment heuristic closes older ongoing stays when new visit starts`() {
        val activities = listOf(
            FirebaseActivity(geofenceId = "place_home", transitionType = "ARRIVED", timestamp = createTimestamp(7, 0)),
            FirebaseActivity(geofenceId = "place_office", transitionType = "ARRIVED", timestamp = createTimestamp(9, 0)),
            FirebaseActivity(geofenceId = "place_office", transitionType = "LEFT", timestamp = createTimestamp(17, 0))
        )

        val replays = repository.buildDayReplays(activities, places)
        val testDate = Instant.ofEpochMilli(createTimestamp(7, 0)).atZone(indiaZone).toLocalDate()
        val dayReplay = replays[testDate]

        assertNotNull(dayReplay)
        assertEquals(2, dayReplay!!.stops.size)

        val adjustedHomeStop = dayReplay.stops[0]
        assertEquals("place_home", adjustedHomeStop.placeId)
        assertEquals(createTimestamp(7, 0), adjustedHomeStop.arrivedAt)
        assertEquals(createTimestamp(9, 0), adjustedHomeStop.departedAt)
        assertEquals(2 * 60 * 60 * 1000L, adjustedHomeStop.stayDurationMillis)
    }

    @Test
    fun `test consecutive duplicate visits are kept separate and chronological`() {
        val activities = listOf(
            FirebaseActivity(geofenceId = "place_home", transitionType = "ARRIVED", timestamp = createTimestamp(7, 0)),
            FirebaseActivity(geofenceId = "place_home", transitionType = "LEFT", timestamp = createTimestamp(8, 0)),
            
            FirebaseActivity(geofenceId = "place_office", transitionType = "ARRIVED", timestamp = createTimestamp(9, 0)),
            FirebaseActivity(geofenceId = "place_office", transitionType = "LEFT", timestamp = createTimestamp(17, 0)),
            
            FirebaseActivity(geofenceId = "place_home", transitionType = "ARRIVED", timestamp = createTimestamp(18, 0)),
            FirebaseActivity(geofenceId = "place_home", transitionType = "LEFT", timestamp = createTimestamp(22, 0))
        )

        val replays = repository.buildDayReplays(activities, places)
        val testDate = Instant.ofEpochMilli(createTimestamp(7, 0)).atZone(indiaZone).toLocalDate()
        val dayReplay = replays[testDate]

        assertNotNull(dayReplay)
        assertEquals(3, dayReplay!!.stops.size)

        assertEquals("place_home", dayReplay.stops[0].placeId)
        assertEquals("place_office", dayReplay.stops[1].placeId)
        assertEquals("place_home", dayReplay.stops[2].placeId)
    }

    @Test
    fun `test time sections classification`() {
        assertEquals(DayReplayTimeSection.MORNING, DayReplayTimeSection.fromTimestamp(createTimestamp(6, 0)))
        assertEquals(DayReplayTimeSection.MORNING, DayReplayTimeSection.fromTimestamp(createTimestamp(11, 59)))

        assertEquals(DayReplayTimeSection.AFTERNOON, DayReplayTimeSection.fromTimestamp(createTimestamp(12, 0)))
        assertEquals(DayReplayTimeSection.AFTERNOON, DayReplayTimeSection.fromTimestamp(createTimestamp(16, 59)))

        assertEquals(DayReplayTimeSection.EVENING, DayReplayTimeSection.fromTimestamp(createTimestamp(17, 0)))
        assertEquals(DayReplayTimeSection.EVENING, DayReplayTimeSection.fromTimestamp(createTimestamp(20, 59)))

        assertEquals(DayReplayTimeSection.NIGHT, DayReplayTimeSection.fromTimestamp(createTimestamp(21, 0)))
        assertEquals(DayReplayTimeSection.NIGHT, DayReplayTimeSection.fromTimestamp(createTimestamp(3, 0)))
    }

    @Test
    fun `test hero story narrative generation`() {
        val activities1 = listOf(
            FirebaseActivity(geofenceId = "place_home", transitionType = "ARRIVED", timestamp = createTimestamp(7, 0)),
            FirebaseActivity(geofenceId = "place_home", transitionType = "LEFT", timestamp = createTimestamp(22, 0))
        )
        val replays1 = repository.buildDayReplays(activities1, places)
        val replay1 = replays1.values.first()
        assertEquals("A peaceful day spent entirely at Home.", replay1.heroNarrative)

        val activities2 = listOf(
            FirebaseActivity(geofenceId = "place_home", transitionType = "ARRIVED", timestamp = createTimestamp(7, 0)),
            FirebaseActivity(geofenceId = "place_home", transitionType = "LEFT", timestamp = createTimestamp(8, 0)),
            FirebaseActivity(geofenceId = "place_office", transitionType = "ARRIVED", timestamp = createTimestamp(9, 0)),
            FirebaseActivity(geofenceId = "place_office", transitionType = "LEFT", timestamp = createTimestamp(17, 0)),
            FirebaseActivity(geofenceId = "place_starbucks", transitionType = "ARRIVED", timestamp = createTimestamp(17, 30)),
            FirebaseActivity(geofenceId = "place_starbucks", transitionType = "LEFT", timestamp = createTimestamp(18, 0))
        )
        val replays2 = repository.buildDayReplays(activities2, places)
        val replay2 = replays2.values.first()
        assertEquals("A journey through Office and 2 other places, spending the majority of your time at Office.", replay2.heroNarrative)
    }

    private fun createTimestamp(hour: Int, minute: Int): Long {
        val today = LocalDate.now(indiaZone)
        return today.atTime(hour, minute).atZone(indiaZone).toInstant().toEpochMilli()
    }
}
