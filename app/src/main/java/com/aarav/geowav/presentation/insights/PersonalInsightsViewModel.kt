package com.aarav.geowav.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.insights.Insights.AverageVisitDurationInsight
import com.aarav.geowav.core.insights.Insights.MostVisitedPlaceInsight
import com.aarav.geowav.core.insights.Insights.WeeklyAwarenessSummaryInsight
import com.aarav.geowav.core.insights.PersonalInsightScope
import com.aarav.geowav.domain.repository.GeoActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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
        observeInsights(PersonalInsightScope.Month)
    }

    fun onScopeChanged(scope: PersonalInsightScope) {
        if (_uiState.value.mostVisitedPlaceScope == scope) return
        observeInsights(scope)
    }

    private data class CombinedInsights(
        val mostVisited: MostVisitedPlaceInsight?,
        val averageDuration: AverageVisitDurationInsight?,
        val awarenessSummary: WeeklyAwarenessSummaryInsight?,
        val weeklySummary: WeeklyAwarenessSummaryInsight?
    )

    private fun observeInsights(scope: PersonalInsightScope) {
        insightJob?.cancel()
        _uiState.update {
            it.copy(
                mostVisitedPlaceScope = scope,
                mostVisitedPlaceInsight = null,
                averageVisitDurationInsight = null,
                awarenessSummaryInsight = null,
                weeklyAwarenessSummaryInsight = null,
                isLoading = true,
                error = null
            )
        }

        insightJob = viewModelScope.launch {
            combine(
                geoActivityRepository.observeMostVisitedPlace(scope),
                geoActivityRepository.observeAverageVisitDuration(scope),
                geoActivityRepository.observeWeeklyAwarenessSummary(scope),
                geoActivityRepository.observeWeeklyAwarenessSummary(PersonalInsightScope.Week)
            ) { mostVisited, avgDuration, awareness, weekly ->
                CombinedInsights(mostVisited, avgDuration, awareness, weekly)
            }
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            mostVisitedPlaceInsight = null,
                            averageVisitDurationInsight = null,
                            awarenessSummaryInsight = null,
                            weeklyAwarenessSummaryInsight = null,
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
                .collectLatest { combined ->
                    _uiState.update {
                        it.copy(
                            mostVisitedPlaceInsight = combined.mostVisited,
                            averageVisitDurationInsight = combined.averageDuration,
                            awarenessSummaryInsight = combined.awarenessSummary,
                            weeklyAwarenessSummaryInsight = combined.weeklySummary,
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
    val averageVisitDurationInsight: AverageVisitDurationInsight? = null,
    val weeklyAwarenessSummaryInsight: WeeklyAwarenessSummaryInsight? = null,
    val awarenessSummaryInsight: WeeklyAwarenessSummaryInsight? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
