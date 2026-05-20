package com.aarav.geowav.presentation.yourplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.FeatureAccess
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.model.UpgradeContext
import com.aarav.geowav.data.model.UpgradeEvents
import com.aarav.geowav.data.model.UpgradeReason
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.domain.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class YourPlacesVM @Inject constructor(
    private val placeRepository: PlaceRepository
) : ViewModel() {
    private val _uiState: MutableStateFlow<YourPlacesUiState> =
        MutableStateFlow(YourPlacesUiState())
    val uiState: StateFlow<YourPlacesUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<UpgradeEvents>()
    val event = _event.asSharedFlow()

    fun deletePlace(place: Place) {
        viewModelScope.launch {
            placeRepository.deletePlace(place)
        }
    }

    fun updatePlaceDetails(place: Place, newName: String, newRadius: Float) {
        viewModelScope.launch {
            val updatedPlace = place.copy(customName = newName, radius = newRadius)
            placeRepository.updatePlace(updatedPlace)
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
                .collectLatest { list ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            placesList = list
                        )
                    }
                }
        }
    }

    fun onPlaceLimitReached(
        plan: UserPlan
    ) {
        viewModelScope.launch {

            val upgradeTo = FeatureAccess.nextPlan(plan) ?: return@launch

            _event.emit(
                UpgradeEvents.ShowUpgrade(
                    UpgradeContext(
                        upgradeTo = upgradeTo,
                        reason = UpgradeReason.MaxPlaces
                    )
                )
            )
        }
    }
}

data class YourPlacesUiState(
    val isLoading: Boolean = false,
    val placesList: List<Place> = emptyList(),
)