package com.aarav.geowav.presentation.timeline

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.data.authentication.GoogleSignInClient
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
    val googleSignInClient: GoogleSignInClient,
    val sessionHistoryRepository: SessionHistoryRepository
) : ViewModel() {

//    private var _sessionHistory = MutableStateFlow<List<SessionHistory>>(emptyList())
//    val sessionHistory: StateFlow<List<SessionHistory>> = _sessionHistory.asStateFlow()

    private var _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    val currentUserId = googleSignInClient.getUserId()

    fun getUserSessions(userId: String) {


        _uiState.update {
            it.copy(
                isLoading = true
            )
        }

        viewModelScope.launch {
            sessionHistoryRepository.getSessionsForUser(userId, currentUserId).collect { list ->
                _uiState.update {
                    it.copy(
                        sessions = list,
                        isLoading = false
                    )
                }
            }
        }
    }

//    fun getMySessions() {
//        if(currentUserId.isNotEmpty()) {
//            _uiState.update {
//                it.copy(
//                    isLoading = true
//                )
//            }
//
//
//            viewModelScope.launch {
//                sessionHistoryRepository.getSessionsForUser(currentUserId).collect { list ->
//
//                    Log.i("SESSIONS", "currentUserId: ${list.toString()}")
//                    _uiState.update {
//                        it.copy(
//                            mySessions = list,
//                            isLoading = false
//                        )
//                    }
//                }
//            }
//        }
//    }

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
    val sessions: List<TimelineItem> = emptyList(),
    val mySessions: List<TimelineItem> = emptyList()
)
