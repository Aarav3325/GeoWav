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
class TimelineMapPreviewVM
@Inject constructor(
    val sessionHistoryRepository: SessionHistoryRepository
) : ViewModel() {

    private var _currentSession = MutableStateFlow<SessionHistory?>(null)
    val currentSession: StateFlow<SessionHistory?> = _currentSession.asStateFlow()

    fun getSessionInfo(sessionId: String) {
        viewModelScope.launch {
            sessionHistoryRepository.getSessionById(sessionId)
                .collect {
                    _currentSession.value = it
                }
        }
    }
}