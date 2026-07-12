package com.aarav.geowav.presentation.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.TimelineItem
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.domain.repository.SessionHistoryRepository
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
class TimelineViewModel
@Inject constructor(
    val googleSignInClient: GoogleSignInClient,
    val sessionHistoryRepository: SessionHistoryRepository,
) : ViewModel() {


    private var _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    val currentUserId = googleSignInClient.getUserId()

    private var observeJob: Job? = null




    fun getMySessions(
        filter: ActivityFilter,
        plan: UserPlan
    ) {
        if (currentUserId.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    isLoading = true
                )
            }


            viewModelScope.launch {
                sessionHistoryRepository.getSessionsForCurrentUser(currentUserId, filter, plan)
                    .collect { list ->


                        _uiState.update {
                            it.copy(
                                mySessions = list,
                                isLoading = false
                            )
                        }
                    }
            }
        }
    }

    fun onFilterChanged(
        newFilter: ActivityFilter, userId: String,
        plan: UserPlan
    ) {
        if (_uiState.value.currentFilter == newFilter) return
        observeForFilter(newFilter, userId, plan)
        getMySessions(newFilter, plan)
    }

    fun observeForFilter(
        filter: ActivityFilter, userId: String,
        plan: UserPlan
    ) {
        observeJob?.cancel()

        _uiState.update {
            it.copy(
                currentFilter = filter,
                isLoading = true,
            )
        }

        observeJob = viewModelScope.launch {
            sessionHistoryRepository.getSessionsVisibleTo(userId, currentUserId, filter, plan)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                }
                .collectLatest { list ->
                    _uiState.update {
                        it.copy(
                            sessions = list,
                            isLoading = false,
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

data class TimelineUiState(
    val isLoading: Boolean = true,
    val sessions: List<TimelineItem> = emptyList(),
    val mySessions: List<TimelineItem> = emptyList(),
    val showDatePicker: Boolean = false,
    val currentFilter: ActivityFilter = ActivityFilter.Today,
    val error: String? = null
)
