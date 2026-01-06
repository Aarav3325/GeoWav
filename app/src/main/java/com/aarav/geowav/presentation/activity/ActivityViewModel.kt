package com.aarav.geowav.presentation.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.data.geofence.ActivityFilter
import com.aarav.geowav.data.model.GeoAlert
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
class ActivityViewModel
@Inject constructor(val activityRepository: GeoActivityRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeForFilter(ActivityFilter.Today)
    }

    fun onFilterChanged(newFilter: ActivityFilter) {
        if (_uiState.value.currentFilter == newFilter) return
        observeForFilter(newFilter)
    }

    fun showDatePicker() {
        _uiState.update {
            it.copy(showDatePicker = true)
        }
    }

    fun dismissDatePicker() {
        _uiState.update {
            it.copy(showDatePicker = false)
        }
    }


    fun observeForFilter(filter: ActivityFilter) {
        observeJob?.cancel()

        _uiState.update {
            it.copy(
                currentFilter = filter,
                isLoading = true,
                error = null
            )
        }

        observeJob = viewModelScope.launch {
            activityRepository.observeAlerts(filter)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message,
                            alerts = emptyList()
                        )
                    }
                }
                .collectLatest { alerts ->
                    _uiState.update {
                        it.copy(
                            alerts = alerts,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }

    }
}

data class ActivityUiState(
    val alerts: List<GeoAlert> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showDatePicker: Boolean = false,
    val currentFilter: ActivityFilter = ActivityFilter.Today
)