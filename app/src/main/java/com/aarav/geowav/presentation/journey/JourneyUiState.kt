package com.aarav.geowav.presentation.journey

import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.DestinationLocation
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.model.SessionMode
import com.aarav.geowav.data.model.SessionStatus
import com.aarav.geowav.data.model.SharingSession

import com.google.android.gms.maps.model.LatLng

enum class JourneyStep {
    DESTINATION_SELECTION,
    MEMBER_SELECTION,
    CONFIRMATION,
    ACTIVE_JOURNEY,
    ARRIVED,
    NOT_REACHED
}

data class JourneyUiState(
    val step: JourneyStep = JourneyStep.DESTINATION_SELECTION,
    val savedPlaces: List<Place> = emptyList(),
    val circleMembers: List<CircleMember> = emptyList(),
    val selectedPlace: Place? = null,
    val customDestination: DestinationLocation? = null,
    val selectedMemberIds: Set<String> = emptySet(),
    val activeSession: SharingSession? = null,
    val distanceToDestinationMeters: Double? = null,
    val journeyDurationMinutes: Int = 0,
    val userLocation: LatLng? = null,
    val isLoadingPlaces: Boolean = false,
    val isLoadingMembers: Boolean = false,
    val isActionLoading: Boolean = false,
    val error: String? = null
) {
    val isOtherSharingActive: Boolean
        get() = activeSession != null &&
                activeSession.mode != SessionMode.JOURNEY &&
                (activeSession.status == SessionStatus.ACTIVE || activeSession.status == SessionStatus.PAUSED)
}

sealed class JourneyUiEvent {
    data class ShowError(val message: String) : JourneyUiEvent()
    object JourneyStarted : JourneyUiEvent()
    object JourneyCompleted : JourneyUiEvent()
    object JourneyCancelled : JourneyUiEvent()
}
