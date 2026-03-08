package com.aarav.geowav.presentation.timeline

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.data.datasource.retrofit.GoogleRoadsRetrofitInstance
import com.aarav.geowav.data.model.SnappedPoint
import com.aarav.geowav.data.model.StayPoint
import com.aarav.geowav.data.model.TimelineItem
import com.aarav.geowav.data.repository.SnapToRoadRepository
import com.aarav.geowav.domain.repository.SessionHistoryRepository
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.MapType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineMapPreviewVM @Inject constructor(
    private val sessionHistoryRepository: SessionHistoryRepository
) : ViewModel() {

    private val snapToRoadRepository = SnapToRoadRepository(
        GoogleRoadsRetrofitInstance.getRoadsApi()
    )

    private val _uiState = MutableStateFlow(TimelinePreviewUiState())
    val uiState: StateFlow<TimelinePreviewUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null


    fun getSessionInfo(sessionId: String, userId: String) {

        Log.i("PLAYBACK", "session info called ${_uiState.value.session}")
        viewModelScope.launch {
            val session = sessionHistoryRepository.getSessionById(sessionId, userId)
            _uiState.update { it.copy(session = session) }
        }
    }


    fun getSnappedPath(path: String, interpolate: Boolean) {
        Log.i("SNAP", "snap res: called")
        viewModelScope.launch {
            when (val result = snapToRoadRepository.snapToRoad(
                path = path,
                interpolate = interpolate
            )) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(snappedPath = result.data ?: emptyList())
                    }
                    Log.i("SNAP", "snap res: " + result.data)
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(snappedPath = emptyList()) }
                    Log.i("SNAP", "snap error: " + result.message)
                }

                else -> Unit
            }
        }
    }


    fun startPlayback() {
        _uiState.update { it.copy(isPlaying = true) }
    }

    fun pausePlayback() {
        _uiState.update { it.copy(isPlaying = false) }
        playbackJob?.cancel()
        playbackJob = null
    }

    fun restartPlayback(startLatLng: LatLng) {
        playbackJob?.cancel()
        playbackJob = null
        _uiState.update {
            it.copy(
                isPlaying = false,
                playbackIndex = 0,
                animatedPath = emptyList(),
                revealedStayPoints = emptyList(),
                lastPosition = null
            )
        }
    }


    fun runPlayback(path: List<LatLng>, stayPoints: List<StayPoint>) {
        playbackJob?.cancel()

        playbackJob = viewModelScope.launch {

            val state = _uiState.value

            for (i in state.playbackIndex until path.size - 1) {

                if (!_uiState.value.isPlaying) return@launch

                val start = _uiState.value.lastPosition ?: path[i]
                val end = path[i + 1]

                val distance = SphericalUtil.computeDistanceBetween(start, end)

                val currentSpeed = _uiState.value.speed
                val duration = (distance / (35.0 * currentSpeed) * 1000).toLong()

                val steps = (duration / 16).toInt().coerceAtLeast(1)

                for (step in 0..steps) {

                    if (!_uiState.value.isPlaying) return@launch

                    val fraction = step / steps.toFloat()

                    val interpolated =
                        SphericalUtil.interpolate(start, end, fraction.toDouble())

                    _uiState.update { current ->
                        current.copy(
                            animatedPath = current.animatedPath + interpolated,
                            lastPosition = interpolated
                        )
                    }

                    delay(duration / steps)
                }

                _uiState.update {
                    it.copy(playbackIndex = i + 1)
                }

                val currentPoint = path[_uiState.value.playbackIndex]

                stayPoints.forEach { stay ->
                    if (_uiState.value.revealedStayPoints.contains(stay)) return@forEach

                    val stayPos = LatLng(stay.lat, stay.lng)
                    val dist = SphericalUtil.computeDistanceBetween(currentPoint, stayPos)

                    if (dist < 30) {
                        _uiState.update { current ->
                            current.copy(
                                revealedStayPoints = current.revealedStayPoints + stay
                            )
                        }
                    }
                }
            }

            _uiState.update { it.copy(isPlaying = false) }
        }
    }


    fun toggleMapType() {
        val next = when (_uiState.value.mapType) {
            MapType.NORMAL -> MapType.SATELLITE
            MapType.SATELLITE -> MapType.TERRAIN
            MapType.TERRAIN -> MapType.HYBRID
            else -> MapType.NORMAL
        }
        _uiState.update { it.copy(mapType = next) }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("PLAYBACK", "onCleared called ${_uiState.value.session}")
        playbackJob?.cancel()
    }
}

data class TimelinePreviewUiState(
    val session: TimelineItem? = null,
    val snappedPath: List<SnappedPoint> = emptyList(),
    val isPlaying: Boolean = false,
    val playbackIndex: Int = 0,
    val speed: Float = 1f,
    val animatedPath: List<LatLng> = emptyList(),
    val revealedStayPoints: List<StayPoint> = emptyList(),
    val mapType: MapType = MapType.NORMAL,
    val lastPosition: LatLng? = null,
    val loading: Boolean = false
)
