package com.aarav.geowav.presentation.releasenotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.data.model.ReleaseNote
import com.aarav.geowav.domain.repository.ReleaseNotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReleaseNotesViewModel @Inject constructor(
    private val repository: ReleaseNotesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReleaseNotesUiState>(ReleaseNotesUiState.Loading)
    val uiState: StateFlow<ReleaseNotesUiState> = _uiState.asStateFlow()

    init {
        loadReleaseNotes()
    }

    fun loadReleaseNotes() {
        viewModelScope.launch {
            _uiState.value = ReleaseNotesUiState.Loading
            try {
                val notes = repository.getReleaseNotes()
                // Sort by version code descending so the newest update is always at the top
                val sortedNotes = notes.sortedByDescending { it.versionCode }
                _uiState.value = ReleaseNotesUiState.Success(sortedNotes)
            } catch (e: Exception) {
                _uiState.value = ReleaseNotesUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}

sealed interface ReleaseNotesUiState {
    object Loading : ReleaseNotesUiState
    data class Success(val notes: List<ReleaseNote>) : ReleaseNotesUiState
    data class Error(val message: String) : ReleaseNotesUiState
}
