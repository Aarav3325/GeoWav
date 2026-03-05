package com.aarav.geowav.presentation.timeline

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.data.datasource.retrofit.GoogleRoadsRetrofitInstance
import com.aarav.geowav.data.model.SnappedPoint
import com.aarav.geowav.data.model.TimelineItem
import com.aarav.geowav.data.repository.SnapToRoadRepository
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

    val snapToRoadRepository = SnapToRoadRepository(
        GoogleRoadsRetrofitInstance.getRoadsApi()
    )

    private var _currentSession = MutableStateFlow<TimelineItem?>(null)
    val currentSession: StateFlow<TimelineItem?> = _currentSession.asStateFlow()

    private var _snappedPath = MutableStateFlow<List<SnappedPoint>>(emptyList())
    val snappedPath: StateFlow<List<SnappedPoint>> = _snappedPath.asStateFlow()

    fun getSessionInfo(sessionId: String, userId: String) {
        viewModelScope.launch {
//            sessionHistoryRepository.getSessionById(sessionId)
//                .collect {
//                    _currentSession.value = it
//                }

            _currentSession.value = sessionHistoryRepository.getSessionById(sessionId, userId)
        }
    }

    fun getSnappedPath(
        path: String,
        interpolate: Boolean,
        apiKey: String
    ) {
        Log.i("SNAP", "snap res: called")
        viewModelScope.launch {
            when (val result = snapToRoadRepository.snapToRoad(
                path = path,
                interpolate = interpolate,
                apiKey = apiKey
            )) {
                is Resource.Success -> {
                    _snappedPath.value = result.data ?: emptyList()
                    Log.i("SNAP", "snap res: " + result.data)
                }

                is Resource.Error -> {
                    _snappedPath.value = emptyList()
                    Log.i("SNAP", "snap error: " + result.message)
                }

                else -> Unit
            }
        }
    }
}

data class TimelinePreviewUiState(
    val loading: Boolean = false,
    val snappedPath: List<SnappedPoint> = emptyList()
)