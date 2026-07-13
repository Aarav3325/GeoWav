package com.aarav.geowav.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.insights.Insights.AverageVisitDurationInsight
import com.aarav.geowav.core.insights.Insights.MostVisitedPlaceInsight
import com.aarav.geowav.core.insights.Insights.WeeklyAwarenessSummaryInsight
import com.aarav.geowav.core.utils.InitialLoadEvent
import com.aarav.geowav.core.utils.NetworkFailure
import com.aarav.geowav.core.insights.PersonalInsightScope
import com.aarav.geowav.core.utils.withInitialLoadTimeout
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
    private val geoActivityRepository: GeoActivityRepository,
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
            val keepExistingContent = it.hasInsightContent
            it.copy(
                mostVisitedPlaceScope = scope,
                mostVisitedPlaceInsight = if (keepExistingContent) it.mostVisitedPlaceInsight else null,
                averageVisitDurationInsight = if (keepExistingContent) it.averageVisitDurationInsight else null,
                awarenessSummaryInsight = if (keepExistingContent) it.awarenessSummaryInsight else null,
                weeklyAwarenessSummaryInsight = if (keepExistingContent) it.weeklyAwarenessSummaryInsight else null,
                isLoading = !keepExistingContent,
                error = null,
                failure = null
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
                .withInitialLoadTimeout()
                .catch { error ->
                    _uiState.update {
                        val hasContent = it.hasInsightContent
                        it.copy(
                            isLoading = false,
                            error = if (hasContent) it.error else "We couldn't load insights right now.",
                            failure = if (hasContent) null else NetworkFailure.ServerError
                        )
                    }
                }
                .collectLatest { event ->
                    when (event) {
                        InitialLoadEvent.TimedOut -> {
                            _uiState.update {
                                if (it.hasInsightContent) {
                                    it.copy(isLoading = false)
                                } else {
                                    it.copy(
                                        isLoading = false,
                                        error = "We're still loading your insights. Please try again.",
                                        failure = NetworkFailure.Timeout
                                    )
                                }
                            }
                        }

                        is InitialLoadEvent.Value -> {
                            val combined = event.value
                            _uiState.update {
                                it.copy(
                                    mostVisitedPlaceInsight = combined.mostVisited,
                                    averageVisitDurationInsight = combined.averageDuration,
                                    awarenessSummaryInsight = combined.awarenessSummary,
                                    weeklyAwarenessSummaryInsight = combined.weeklySummary,
                                    isLoading = false,
                                    error = null,
                                    failure = null
                                )
                            }
                        }
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
    val error: String? = null,
    val failure: NetworkFailure? = null
) {
    val hasInsightContent: Boolean
        get() = mostVisitedPlaceInsight != null ||
            averageVisitDurationInsight != null ||
            weeklyAwarenessSummaryInsight != null ||
            awarenessSummaryInsight != null
}
