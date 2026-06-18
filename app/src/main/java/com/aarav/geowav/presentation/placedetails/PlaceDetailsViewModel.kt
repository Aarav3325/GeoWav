package com.aarav.geowav.presentation.placedetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.insights.PlaceActivityMetricsCalculator
import com.aarav.geowav.data.mapper.FirebaseActivity
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.domain.repository.GeoActivityRepository
import com.aarav.geowav.domain.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
                    place = place,
                    placeName = place.customName.ifBlank { place.placeName },
                    radius = place.radius
                )
            }

            geoActivityRepository.observeActivityHistory()
                .collectLatest { activities ->
                    val metrics = PlaceActivityMetricsCalculator.calculate(place, activities)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEmptyState = metrics.isEmptyState,
                            relativeLastSeenText = metrics.relativeLastSeenText,
                            lastSeenValue = metrics.lastSeenValue,
                            visitsValue = metrics.visitsValue,
                            averageStayValue = metrics.averageStayValue
                        )
                    }
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
}
