package com.aarav.geowav.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.insights.MostVisitedPlaceInsight
import com.aarav.geowav.core.insights.PersonalInsightScope
import com.aarav.geowav.domain.repository.GeoActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonalInsightsViewModel @Inject constructor(
    private val geoActivityRepository: GeoActivityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PersonalInsightsUiState())
    val uiState: StateFlow<PersonalInsightsUiState> = _uiState.asStateFlow()

    private var insightJob: Job? = null

    init {
        observeMostVisitedPlace(PersonalInsightScope.Month)
    }

    fun onMostVisitedPlaceScopeChanged(scope: PersonalInsightScope) {
        if (_uiState.value.mostVisitedPlaceScope == scope) return
        observeMostVisitedPlace(scope)
    }

    private fun observeMostVisitedPlace(scope: PersonalInsightScope) {
        insightJob?.cancel()
        _uiState.update {
            it.copy(
                mostVisitedPlaceScope = scope,
                mostVisitedPlaceInsight = null,
                isLoading = true,
                error = null
            )
        }

        insightJob = viewModelScope.launch {
            geoActivityRepository.observeMostVisitedPlace(scope)
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            mostVisitedPlaceInsight = null,
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
                .collectLatest { insight ->
                    _uiState.update {
                        it.copy(
                            mostVisitedPlaceInsight = insight,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
}

data class PersonalInsightsUiState(
    val mostVisitedPlaceScope: PersonalInsightScope = PersonalInsightScope.Month,
    val mostVisitedPlaceInsight: MostVisitedPlaceInsight? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
