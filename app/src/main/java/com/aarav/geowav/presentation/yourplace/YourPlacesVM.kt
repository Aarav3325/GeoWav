package com.aarav.geowav.presentation.yourplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.data.place.Place
import com.aarav.geowav.domain.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class YourPlacesVM @Inject constructor(
    private val placeRepository: PlaceRepository
): ViewModel() {
    private val _uiState: MutableStateFlow<YourPlacesUiState> = MutableStateFlow(YourPlacesUiState())
    val uiState: StateFlow<YourPlacesUiState> = _uiState.asStateFlow()

    fun deletePlace(place: Place) {
        viewModelScope.launch {
            placeRepository.deletePlace(place)
        }
    }

    fun getPlaces() {
        _uiState.update {
            it.copy(
                isLoading = true
            )
        }

        viewModelScope.launch {
            placeRepository.getPlaces()
                .collectLatest {
                    list ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            placesList = list
                        )
                    }
                }
        }
    }
}

data class YourPlacesUiState(
    val isLoading: Boolean = false,
    val placesList: List<Place> = emptyList(),
)