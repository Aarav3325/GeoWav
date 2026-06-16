package com.aarav.geowav.presentation.placedetails

data class PlaceDetailsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val place: com.aarav.geowav.data.model.Place? = null,
    val placeName: String = "",
    val radius: Float = 0f,
    val relativeLastSeenText: String = "",
    val lastSeenValue: String = "--",
    val lastSeenLabel: String = "Last seen",
    val visitsValue: String = "0",
    val visitsLabel: String = "Visits",
    val averageStayValue: String = "--",
    val averageStayLabel: String = "Average stay",
    val isEmptyState: Boolean = false
)
