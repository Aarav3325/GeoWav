package com.aarav.geowav.presentation.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.domain.repository.PlaceRepository
import com.aarav.geowav.core.utils.Resource
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(application: Application,
   private val placeRepository: PlaceRepository) : AndroidViewModel(application) {



    private var _uiState = MutableStateFlow<MapScreenUiState>(MapScreenUiState())
    val uiState: StateFlow<MapScreenUiState> = _uiState.asStateFlow()

    fun showBottomSheet() {
        if (_uiState.value.selectedPlace != null){
            _uiState.update {
                it.copy(
                    isBottomSheetShowing = true
                )
            }
        }
    }

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
                            isLoading = false,
                            isBottomSheetShowing = true,
                            isSearchExpanded = false,
                            predictions = emptyList()
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


    fun searchPlaces(query: String) {

        if (_uiState.value.isLoading) return
        if (_uiState.value.showErrorDialog) return

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    predictions = emptyList()
                )
            }

            when(val result = placeRepository.searchPlaces(query)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            predictions = result.data ?: emptyList()
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState
                        .update {
                            it.copy(
                                isLoading = false,
                                showErrorDialog = true,
                                error = result.message ?: "Unable to find matching places"
                            )
                        }
                }

                is Resource.Loading -> {

                }
            }
        }
    }

    fun onExpandChange(flag: Boolean) {
        _uiState.update {
            it.copy(
                isSearchExpanded = flag
            )
        }
    }

    fun dismissBottomSheet() {
        _uiState.update {
            it.copy(
                isBottomSheetShowing = false
            )
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


}

data class MapScreenUiState(
    val isSearchExpanded: Boolean = false,
    val isBottomSheetShowing: Boolean = false,
    val selectedPlace: Place? = null,
    val showErrorDialog: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val predictions: List<AutocompletePrediction> = emptyList()
)