package com.aarav.geowav.presentation.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.data.model.SessionHistory
import com.aarav.geowav.domain.repository.SessionHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel
@Inject constructor(
    val sessionHistoryRepository: SessionHistoryRepository
) : ViewModel() {

    private var _sessionHistory = MutableStateFlow<List<SessionHistory>>(emptyList())
    val sessionHistory: StateFlow<List<SessionHistory>> = _sessionHistory.asStateFlow()

    fun getUserSessionHistory(userId: String) {
        viewModelScope.launch {
            sessionHistoryRepository.getSessionsForUser(userId)
                .collect {
                    _sessionHistory.value = it
                }
        }
    }
}