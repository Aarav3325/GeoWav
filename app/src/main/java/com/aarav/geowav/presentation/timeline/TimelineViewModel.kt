package com.aarav.geowav.presentation.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.data.model.SessionHistory
import com.aarav.geowav.data.model.TimelineItem
import com.aarav.geowav.domain.repository.SessionHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel
@Inject constructor(
    val sessionHistoryRepository: SessionHistoryRepository
) : ViewModel() {

//    private var _sessionHistory = MutableStateFlow<List<SessionHistory>>(emptyList())
//    val sessionHistory: StateFlow<List<SessionHistory>> = _sessionHistory.asStateFlow()

    private var _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    fun getUserSessions(userId: String) {


        _uiState.update {
            it.copy(
                isLoading = true
            )
        }

        viewModelScope.launch {
            sessionHistoryRepository.getSessionForUserFirebase(userId).collect { list ->
                _uiState.update {
                    it.copy(
                        sessionsFirebase = list,
                        isLoading = false
                    )
                }
            }
        }
    }

//    fun getUserSessionHistory(userId: String) {
//
//
//
//        viewModelScope.launch {
//            sessionHistoryRepository.getSessionsForUser(userId).collect { list ->
//                _uiState.update {
//                    it.copy(
//                        sessions = list,
//                        isLoading = false
//                    )
//                }
//            }
//        }
//
//    }
}

data class TimelineUiState(
    val isLoading: Boolean = true,
    val sessions: List<SessionHistory> = emptyList(),
    val sessionsFirebase: List<TimelineItem> = emptyList()
)
