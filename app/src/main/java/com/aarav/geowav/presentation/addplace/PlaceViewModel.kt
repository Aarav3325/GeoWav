package com.aarav.geowav.presentation.addplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.FeatureAccess
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.model.UpgradeContext
import com.aarav.geowav.data.model.UpgradeEvents
import com.aarav.geowav.data.model.UpgradeReason
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.domain.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaceViewModel @Inject constructor(
    val placeRepository: PlaceRepository
) : ViewModel() {
    val allPlaces: StateFlow<List<Place>> = placeRepository.getPlaces()
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

    private val _uiState: MutableStateFlow<AddPlaceScreenUiState> =
        MutableStateFlow(AddPlaceScreenUiState())
    val uiState: StateFlow<AddPlaceScreenUiState> = _uiState.asStateFlow()


    private val _events = MutableSharedFlow<UpgradeEvents>()
    val events = _events.asSharedFlow()

    fun addPlace(place: Place, userPlan: UserPlan) {
        viewModelScope.launch {

            val max = FeatureAccess.maxSavedPlaces(userPlan)

            if (allPlaces.value.size >= max) {

                val upgradeTo = when (userPlan) {
                    UserPlan.FREE -> UserPlan.PREMIUM
                    UserPlan.PREMIUM -> UserPlan.PRO
                    UserPlan.PRO -> null
                }

                upgradeTo?.let {
                    _events.emit(
                        UpgradeEvents.ShowUpgrade(
                            UpgradeContext(
                                upgradeTo = it,
                                reason = UpgradeReason.MaxPlaces
                            )
                        )
                    )
                }

                return@launch
            }

            placeRepository.addPlace(place)
        }
    }

    fun deletePlace(place: Place) {
        viewModelScope.launch {
            placeRepository.deletePlace(place)
        }
    }

//    fun searchPlaces(
//        query: String
//    ) {
//        val token = AutocompleteSessionToken.newInstance()
//
//        val request = FindAutocompletePredictionsRequest.builder()
//            .setQuery(query)
//            .setSessionToken(token)
//            .build()
//
//
//        placesClient.findAutocompletePredictions(request)
//            .addOnSuccessListener { response ->
//                onResult(response.autocompletePredictions)
//            }
//            .addOnFailureListener { exception ->
//                exception.printStackTrace()
//                onResult(emptyList())
//            }
//    }

    fun fetchPlace(placeId: String) {

        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                showErrorDialog = false
            )
        }

        viewModelScope.launch {
            when (val result = placeRepository.fetchPlace(placeId)) {

                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            selectedPlace = result.data,
                            isLoading = false
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message,
                            showErrorDialog = true
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(
                error = null,
                showErrorDialog = false
            )
        }
    }

    fun onRadiusChange(newRadius: Float) {
        _uiState.update {
            it.copy(
                selectedRadius = newRadius
            )
        }
    }

}

data class AddPlaceScreenUiState(
    val selectedPlace: com.google.android.libraries.places.api.model.Place? = null,
    val selectedRadius: Float = 200F,
    val chips: List<Float> = listOf(200F, 300F, 400F, 500F),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showErrorDialog: Boolean = false
)
