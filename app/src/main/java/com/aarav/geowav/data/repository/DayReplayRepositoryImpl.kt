package com.aarav.geowav.data.repository

import com.aarav.geowav.core.utils.toLocalDateInIndia
import com.aarav.geowav.data.mapper.FirebaseActivity
import com.aarav.geowav.data.model.ActivityTransition
import com.aarav.geowav.data.model.DayReplay
import com.aarav.geowav.data.model.DayReplayStop
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.domain.repository.DayReplayRepository
import com.aarav.geowav.domain.repository.GeoActivityRepository
import com.aarav.geowav.domain.repository.PlaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DayReplayRepositoryImpl @Inject constructor(
    private val geoActivityRepository: GeoActivityRepository,
    private val placeRepository: PlaceRepository
) : DayReplayRepository {

    private data class MatchedActivity(
        val activity: FirebaseActivity,
        val place: Place,
        val transition: ActivityTransition,
        val timestamp: Long
    )

    override fun observeDayReplays(): Flow<Map<LocalDate, DayReplay>> {
        return combine(
            geoActivityRepository.observeActivityHistory(),
            placeRepository.getPlaces()
        ) { activities, savedPlaces ->
            buildDayReplays(activities, savedPlaces)
        }
    }

    internal fun buildDayReplays(
        activities: List<FirebaseActivity>,
        savedPlaces: List<Place>
    ): Map<LocalDate, DayReplay> {
        val matchedActivities = activities.mapNotNull { activity ->
            val timestamp = activity.timestamp?.takeIf { it > 0L } ?: return@mapNotNull null
            val transition = ActivityTransition.fromRaw(
                activity.normalizedTransitionType ?: activity.transitionType
            ) ?: return@mapNotNull null

            val matchedPlace = findMatchingPlace(activity, savedPlaces) ?: return@mapNotNull null

            MatchedActivity(
                activity = activity,
                place = matchedPlace,
                transition = transition,
                timestamp = timestamp
            )
        }

        val sortedActivities = matchedActivities.sortedBy { it.timestamp }

        val visits = mutableListOf<DayReplayStop>()
        val activitiesByPlace = sortedActivities.groupBy { it.place.placeId }

        for ((_, placeEvents) in activitiesByPlace) {
            val place = placeEvents.first().place
            var pendingArrival: MatchedActivity? = null

            for (event in placeEvents) {
                if (event.transition == ActivityTransition.ARRIVED) {
                    if (pendingArrival != null) {
                        visits.add(
                            DayReplayStop(
                                placeId = place.placeId,
                                placeName = place.customName.ifEmpty { place.placeName },
                                address = place.address,
                                latitude = place.latitude,
                                longitude = place.longitude,
                                radius = place.radius,
                                arrivedAt = pendingArrival.timestamp,
                                departedAt = null,
                                stayDurationMillis = null
                            )
                        )
                    }
                    pendingArrival = event
                } else if (event.transition == ActivityTransition.LEFT) {
                    if (pendingArrival != null) {
                        val duration = event.timestamp - pendingArrival.timestamp
                        visits.add(
                            DayReplayStop(
                                placeId = place.placeId,
                                placeName = place.customName.ifEmpty { place.placeName },
                                address = place.address,
                                latitude = place.latitude,
                                longitude = place.longitude,
                                radius = place.radius,
                                arrivedAt = pendingArrival.timestamp,
                                departedAt = event.timestamp,
                                stayDurationMillis = if (duration > 0) duration else 0L
                            )
                        )
                        pendingArrival = null
                    }
                }
            }

            if (pendingArrival != null) {
                visits.add(
                    DayReplayStop(
                        placeId = place.placeId,
                        placeName = place.customName.ifEmpty { place.placeName },
                        address = place.address,
                        latitude = place.latitude,
                        longitude = place.longitude,
                        radius = place.radius,
                        arrivedAt = pendingArrival.timestamp,
                        departedAt = null,
                        stayDurationMillis = null
                    )
                )
            }
        }

        val sortedVisits = visits.sortedBy { it.arrivedAt }

        val adjustedVisits = mutableListOf<DayReplayStop>()
        for (i in sortedVisits.indices) {
            val current = sortedVisits[i]
            if (current.departedAt == null) {
                val nextVisit = sortedVisits.subList(i + 1, sortedVisits.size).firstOrNull()
                if (nextVisit != null) {
                    val duration = nextVisit.arrivedAt - current.arrivedAt
                    adjustedVisits.add(
                        current.copy(
                            departedAt = nextVisit.arrivedAt,
                            stayDurationMillis = if (duration > 0) duration else 0L
                        )
                    )
                } else {
                    adjustedVisits.add(current)
                }
            } else {
                adjustedVisits.add(current)
            }
        }

        val visitsByDate = adjustedVisits.groupBy { it.arrivedAt.toLocalDateInIndia() }

        return visitsByDate.mapValues { (date, stops) ->
            generateHeroStory(date, stops.sortedBy { it.arrivedAt })
        }
    }

    private fun findMatchingPlace(activity: FirebaseActivity, savedPlaces: List<Place>): Place? {
        val id = activity.geofenceId?.trim() ?: ""
        val name = activity.placeName?.trim() ?: ""
        if (id.isEmpty() && name.isEmpty()) return null
        return savedPlaces.find { place ->
            place.placeId == id ||
            (place.customName.isNotEmpty() && (place.customName.equals(id, ignoreCase = true) || place.customName.equals(name, ignoreCase = true))) ||
            (place.placeName.isNotEmpty() && (place.placeName.equals(id, ignoreCase = true) || place.placeName.equals(name, ignoreCase = true)))
        }
    }

    private fun generateHeroStory(date: LocalDate, stops: List<DayReplayStop>): DayReplay {
        val today = LocalDate.now(ZoneId.of("Asia/Kolkata"))
        val isToday = date == today
        val isYesterday = date == today.minusDays(1)

        val title = when {
            isToday -> "Today"
            isYesterday -> "Yesterday"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM")
                date.format(formatter)
            }
        }

        val subtitle = when {
            isToday -> "A calm look at your day so far."
            else -> "A calm look back at your day."
        }

        val narrative = when {
            stops.isEmpty() -> {
                "No saved places visited."
            }
            else -> {
                val uniquePlacesCount = stops.map { it.placeId }.distinct().size
                val placesText = if (uniquePlacesCount == 1) "1 place" else "$uniquePlacesCount places"

                val longestStay = stops
                    .filter { it.stayDurationMillis != null }
                    .groupBy { it.placeId }
                    .mapValues { (_, stopsForPlace) -> stopsForPlace.sumOf { it.stayDurationMillis ?: 0L } }
                    .maxByOrNull { it.value }

                val placeNameForLongestStay = longestStay?.let { entry ->
                    stops.firstOrNull { it.placeId == entry.key }?.placeName
                }

                if (placeNameForLongestStay != null) {
                    if (placeNameForLongestStay.equals("Home", ignoreCase = true)) {
                        "Mostly spent around Home. Visited $placesText."
                    } else {
                        "Visited $placesText. Spent most of your day at $placeNameForLongestStay."
                    }
                } else {
                    "Visited $placesText."
                }
            }
        }

        return DayReplay(
            date = date,
            heroTitle = title,
            heroSubtitle = subtitle,
            heroNarrative = narrative,
            stops = stops
        )
    }
}
