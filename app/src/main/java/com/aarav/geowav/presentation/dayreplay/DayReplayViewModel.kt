package com.aarav.geowav.presentation.dayreplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.data.model.DayReplay
import com.aarav.geowav.domain.repository.DayReplayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class DayReplayViewModel @Inject constructor(
    private val dayReplayRepository: DayReplayRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DayReplayUiState())
    val uiState: StateFlow<DayReplayUiState> = _uiState.asStateFlow()

    init {
        val today = LocalDate.now(ZoneId.of("Asia/Kolkata"))
        val dateList = (0..29).map { today.minusDays(it.toLong()) }.reversed()
        _uiState.update { it.copy(dates = dateList, selectedDate = today) }

        viewModelScope.launch {
            dayReplayRepository.observeDayReplays().collectLatest { replays ->
                _uiState.update {
                    it.copy(
                        dayReplays = replays,
                        isLoading = false
                    )
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
    val dates: List<LocalDate> = emptyList()
)
