package com.aarav.geowav.presentation.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.core.utils.InitialLoadEvent
import com.aarav.geowav.core.utils.NetworkFailure
import com.aarav.geowav.core.utils.withInitialLoadTimeout
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
                val hasContent = it.mySessions.isNotEmpty()
                it.copy(
                    isLoading = !hasContent,
                    error = null,
                    failure = null
                )
            }


            viewModelScope.launch {
                sessionHistoryRepository.getSessionsForCurrentUser(currentUserId, filter, plan)
                    .withInitialLoadTimeout()
                    .collect { event ->
                        when (event) {
                            InitialLoadEvent.TimedOut -> {
                                _uiState.update {
                                    if (it.mySessions.isNotEmpty()) {
                                        it.copy(isLoading = false)
                                    } else {
                                        it.copy(
                                            isLoading = false,
                                            error = "We're still loading your timeline. Please try again.",
                                            failure = NetworkFailure.Timeout
                                        )
                                    }
                                }
                            }

                            is InitialLoadEvent.Value -> {
                                _uiState.update {
                                    it.copy(
                                        mySessions = event.value,
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
            val keepExistingContent = it.currentFilter == filter && it.sessions.isNotEmpty()
            it.copy(
                currentFilter = filter,
                sessions = if (keepExistingContent) it.sessions else emptyList(),
                isLoading = !keepExistingContent,
                error = null,
                failure = null
            )
        }

        observeJob = viewModelScope.launch {
            sessionHistoryRepository.getSessionsVisibleTo(userId, currentUserId, filter, plan)
                .withInitialLoadTimeout()
                .catch { e ->
                    _uiState.update {
                        val hasContent = it.sessions.isNotEmpty()
                        it.copy(
                            isLoading = false,
                            error = if (hasContent) it.error else "We couldn't load your timeline right now.",
                            failure = if (hasContent) null else NetworkFailure.ServerError
                        )
                    }
                }
                .collectLatest { event ->
                    when (event) {
                        InitialLoadEvent.TimedOut -> {
                            _uiState.update {
                                if (it.sessions.isNotEmpty()) {
                                    it.copy(isLoading = false)
                                } else {
                                    it.copy(
                                        isLoading = false,
                                        error = "We're still loading your timeline. Please try again.",
                                        failure = NetworkFailure.Timeout
                                    )
                                }
                            }
                        }

                        is InitialLoadEvent.Value -> {
                            _uiState.update {
                                it.copy(
                                    sessions = event.value,
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
    val error: String? = null,
    val failure: NetworkFailure? = null
)
