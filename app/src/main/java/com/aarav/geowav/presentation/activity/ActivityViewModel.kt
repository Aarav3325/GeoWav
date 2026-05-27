package com.aarav.geowav.presentation.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.CircleActivityItem
import com.aarav.geowav.data.repository.CircleActivityFeedRepository
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
@Inject constructor(
    private val circleActivityFeedRepository: CircleActivityFeedRepository,
    googleSignInClient: GoogleSignInClient
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()
    val viewerId: String = googleSignInClient.getUserId()

    private var observeJob: Job? = null

    init {
        observeForFilter(ActivityFilter.Today)
    }

    fun onFilterChanged(newFilter: ActivityFilter) {
        if (_uiState.value.currentFilter == newFilter) return
        observeForFilter(newFilter)
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
            circleActivityFeedRepository.observeActivity(filter)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message,
                            activities = emptyList()
                        )
                    }
                }
                .collectLatest { activities ->
                    _uiState.update {
                        it.copy(
                            activities = activities,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }

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
}

data class ActivityUiState(
    val activities: List<CircleActivityItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showDatePicker: Boolean = false,
    val currentFilter: ActivityFilter = ActivityFilter.Today
)
