package com.aarav.geowav.presentation.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.CircleActivityItem
import com.aarav.geowav.data.repository.CircleActivityFeedRepository
import com.aarav.geowav.data.repository.CircleActivityFeedRepository.Companion.ACTIVITY_PAGE_SIZE
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
    googleSignInClient: GoogleSignInClient,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()
    val viewerId: String = googleSignInClient.getUserId()

    private var observeJob: Job? = null
    private var olderPageItems: List<CircleActivityItem> = emptyList()

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
                activities = emptyList(),
                isLoading = true,
                isLoadingMore = false,
                hasMore = true,
                error = null
            )
        }
        olderPageItems = emptyList()

        observeJob = viewModelScope.launch {
            circleActivityFeedRepository.observeActivityPage(filter)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = e.message,
                            activities = emptyList()
                        )
                    }
                }
                .collectLatest { activities ->
                    val mergedActivities = mergeActivities(activities, olderPageItems)
                    _uiState.update {
                        it.copy(
                            activities = mergedActivities,
                            oldestLoadedTimestamp = mergedActivities.minOfOrNull { item -> item.timestamp },
                            hasMore = activities.size == ACTIVITY_PAGE_SIZE,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }

    }

    fun loadMore() {
        val state = _uiState.value
        val oldestTimestamp = state.oldestLoadedTimestamp ?: return
        if (state.isLoading || state.isLoadingMore || !state.hasMore) return

        _uiState.update {
            it.copy(
                isLoadingMore = true,
                loadMoreError = null
            )
        }

        viewModelScope.launch {
            runCatching {
                circleActivityFeedRepository.loadOlderActivityPage(
                    filter = state.currentFilter,
                    olderThanTimestamp = oldestTimestamp
                )
            }.onSuccess { olderItems ->
                olderPageItems = mergeActivities(olderPageItems, olderItems)
                val currentState = _uiState.value
                val mergedActivities = mergeActivities(currentState.activities, olderItems)
                _uiState.update {
                    it.copy(
                        activities = mergedActivities,
                        oldestLoadedTimestamp = mergedActivities.minOfOrNull { item -> item.timestamp },
                        isLoadingMore = false,
                        hasMore = olderItems.size == ACTIVITY_PAGE_SIZE,
                        loadMoreError = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        loadMoreError = error.message
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

    private fun mergeActivities(
        currentItems: List<CircleActivityItem>,
        newItems: List<CircleActivityItem>
    ): List<CircleActivityItem> {
        return (currentItems + newItems)
            .distinctBy { item ->
                listOf(
                    item.actorId,
                    item.placeName,
                    item.normalizedTransitionType,
                    item.timestamp
                ).joinToString("|")
            }
            .sortedByDescending { it.timestamp }
    }
}

data class ActivityUiState(
    val activities: List<CircleActivityItem> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val oldestLoadedTimestamp: Long? = null,
    val error: String? = null,
    val loadMoreError: String? = null,
    val showDatePicker: Boolean = false,
    val currentFilter: ActivityFilter = ActivityFilter.Today
)
