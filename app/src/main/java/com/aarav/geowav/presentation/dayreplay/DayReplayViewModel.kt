package com.aarav.geowav.presentation.dayreplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.InitialLoadEvent
import com.aarav.geowav.core.utils.NetworkFailure
import com.aarav.geowav.core.utils.withInitialLoadTimeout
import com.aarav.geowav.data.model.DayReplay
import com.aarav.geowav.domain.repository.DayReplayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class DayReplayViewModel @Inject constructor(
    private val dayReplayRepository: DayReplayRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DayReplayUiState())
    val uiState: StateFlow<DayReplayUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init {
        val today = LocalDate.now(ZoneId.of("Asia/Kolkata"))
        val dateList = (0..29).map { today.minusDays(it.toLong()) }.reversed()
        _uiState.update { it.copy(dates = dateList, selectedDate = today) }

        observeDayReplays()
    }

    fun retry() {
        observeDayReplays()
    }

    private fun observeDayReplays() {
        observeJob?.cancel()
        _uiState.update {
            it.copy(
                isLoading = it.dayReplays.isEmpty(),
                error = null,
                failure = null
            )
        }

        observeJob = viewModelScope.launch {
            dayReplayRepository.observeDayReplays()
                .withInitialLoadTimeout()
                .collectLatest { event ->
                    when (event) {
                        InitialLoadEvent.TimedOut -> {
                            _uiState.update {
                                if (it.dayReplays.isNotEmpty()) {
                                    it.copy(isLoading = false)
                                } else {
                                    it.copy(
                                        isLoading = false,
                                        error = "We're still loading your replay. Please try again.",
                                        failure = NetworkFailure.Timeout
                                    )
                                }
                            }
                        }

                        is InitialLoadEvent.Value -> {
                            _uiState.update {
                                it.copy(
                                    dayReplays = event.value,
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

    fun selectDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                expandedStopIds = emptySet()
            )
        }
    }

    fun toggleStopExpanded(stopId: String) {
        _uiState.update { state ->
            val newExpanded = if (state.expandedStopIds.contains(stopId)) {
                state.expandedStopIds - stopId
            } else {
                state.expandedStopIds + stopId
            }
            state.copy(expandedStopIds = newExpanded)
        }
    }
}

data class DayReplayUiState(
    val isLoading: Boolean = true,
    val dayReplays: Map<LocalDate, DayReplay> = emptyMap(),
    val selectedDate: LocalDate = LocalDate.now(ZoneId.of("Asia/Kolkata")),
    val expandedStopIds: Set<String> = emptySet(),
    val dates: List<LocalDate> = emptyList(),
    val error: String? = null,
    val failure: NetworkFailure? = null
)
