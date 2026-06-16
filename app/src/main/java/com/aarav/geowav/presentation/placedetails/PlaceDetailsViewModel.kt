package com.aarav.geowav.presentation.placedetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.data.mapper.FirebaseActivity
import com.aarav.geowav.data.model.ActivityTransition
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.domain.repository.GeoActivityRepository
import com.aarav.geowav.domain.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@HiltViewModel
class PlaceDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val placeRepository: PlaceRepository,
    private val geoActivityRepository: GeoActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaceDetailsUiState())
    val uiState: StateFlow<PlaceDetailsUiState> = _uiState.asStateFlow()

    val placeId: String = savedStateHandle.get<String>("placeId").orEmpty()

    init {
        loadPlaceDetailsAndHistory()
    }

    private fun loadPlaceDetailsAndHistory() {
        if (placeId.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Invalid Place ID"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val place = placeRepository.getPlaceById(placeId)
            if (place == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Place not found"
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    placeName = place.customName.ifBlank { place.placeName },
                    radius = place.radius
                )
            }

            geoActivityRepository.observeActivityHistory()
                .collectLatest { activities ->
                    val updatedState = calculateMetrics(place, activities)
                    _uiState.update { updatedState }
                }
        }
    }

    fun deletePlace(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val place = placeRepository.getPlaceById(placeId)
            if (place != null) {
                placeRepository.deletePlace(place)
                onSuccess()
            }
        }
    }

    fun updatePlaceDetails(newName: String, newRadius: Float) {
        viewModelScope.launch {
            val place = placeRepository.getPlaceById(placeId) ?: return@launch
            val updatedPlace = place.copy(customName = newName, radius = newRadius)
            placeRepository.updatePlace(updatedPlace)

            _uiState.update {
                it.copy(
                    placeName = updatedPlace.customName.ifBlank { updatedPlace.placeName },
                    radius = updatedPlace.radius
                )
            }
        }
    }

    private fun calculateMetrics(place: Place, activities: List<FirebaseActivity>): PlaceDetailsUiState {
        val matchingActivities = activities.filter { activity ->
            val timestamp = activity.timestamp ?: 0L
            if (timestamp <= 0L) return@filter false

            val activityName = activity.placeName?.trim() ?: activity.geofenceId?.trim() ?: return@filter false
            val targetName = place.customName.ifBlank { place.placeName }.trim()

            activityName.equals(targetName, ignoreCase = true) ||
                    (place.customName.isNotBlank() && activityName.equals(place.customName.trim(), ignoreCase = true)) ||
                    (place.placeName.isNotBlank() && activityName.equals(place.placeName.trim(), ignoreCase = true))
        }.sortedBy { it.timestamp ?: 0L }

        if (matchingActivities.isEmpty()) {
            return _uiState.value.copy(
                isLoading = false,
                isEmptyState = true,
                relativeLastSeenText = "Never visited yet",
                lastSeenValue = "--",
                visitsValue = "0",
                averageStayValue = "--"
            )
        }

        // 1. Last Visit
        val lastActivity = matchingActivities.last()
        val lastVisitTimestamp = lastActivity.timestamp ?: 0L
        val (relativeText, shortText) = if (lastVisitTimestamp > 0L) {
            "Last seen ${formatRelativeTime(lastVisitTimestamp)}" to formatGlanceableTime(lastVisitTimestamp)
        } else {
            "Never visited yet" to "--"
        }

        // 2. Visits This Month
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val startOfMonth = today.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfMonth = today.plusMonths(1).withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val arrivalsThisMonthCount = matchingActivities.count { activity ->
            val ts = activity.timestamp ?: 0L
            val transition = ActivityTransition.fromRaw(activity.normalizedTransitionType ?: activity.transitionType)
            transition == ActivityTransition.ARRIVED && ts >= startOfMonth && ts < endOfMonth
        }

        // 3. Average Stay
        var openArrivalTimestamp: Long? = null
        val completedDurations = mutableListOf<Long>()
        for (activity in matchingActivities) {
            val ts = activity.timestamp ?: continue
            if (ts <= 0L) continue
            val transition = ActivityTransition.fromRaw(activity.normalizedTransitionType ?: activity.transitionType) ?: continue

            when (transition) {
                ActivityTransition.ARRIVED -> {
                    openArrivalTimestamp = ts
                }
                ActivityTransition.LEFT -> {
                    if (openArrivalTimestamp != null) {
                        val duration = ts - openArrivalTimestamp
                        if (duration > 0L) {
                            completedDurations.add(duration)
                        }
                    }
                    openArrivalTimestamp = null
                }
            }
        }

        val averageStayText = if (completedDurations.isNotEmpty()) {
            val avgMillis = completedDurations.average().toLong()
            formatStayDuration(avgMillis)
        } else {
            "--"
        }

        return _uiState.value.copy(
            isLoading = false,
            isEmptyState = false,
            relativeLastSeenText = relativeText,
            lastSeenValue = shortText,
            visitsValue = arrivalsThisMonthCount.toString(),
            averageStayValue = averageStayText
        )
    }

    private fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        if (diff < 0) return "just now"
        val minutes = diff / 60_000
        val hours = diff / 3_600_000
        val days = diff / 86_400_000

        return when {
            minutes < 1 -> "just now"
            minutes < 60 -> "$minutes min${if (minutes > 1) "s" else ""} ago"
            hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
            days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
            else -> {
                val sdf = java.text.SimpleDateFormat("dd MMM, h:mm a", java.util.Locale.getDefault())
                "on ${sdf.format(java.util.Date(timestamp))}"
            }
        }
    }

    private fun formatGlanceableTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        if (diff < 0) return "Now"
        val minutes = diff / 60_000
        val hours = diff / 3_600_000
        val days = diff / 86_400_000

        return when {
            minutes < 1 -> "Now"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            else -> "${days}d"
        }
    }

    private fun formatStayDuration(millis: Long): String {
        val totalMins = millis / 60_000
        val hours = totalMins / 60
        val mins = totalMins % 60
        return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    }
}
