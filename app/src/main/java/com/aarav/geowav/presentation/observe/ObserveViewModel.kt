package com.aarav.geowav.presentation.observe

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.core.utils.ViewerLocationState
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.User
import com.aarav.geowav.domain.repository.CircleRepository
import com.aarav.geowav.domain.repository.ViewerLocationRepository
import com.aarav.geowav.presentation.home.HomeScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.contains

@HiltViewModel
class ObserveViewModel
@Inject constructor(
    val circleRepository: CircleRepository,
    private val viewerLocationRepository: ViewerLocationRepository,
    val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    val viewerId = googleSignInClient.getUserId()

    private val _uiState: MutableStateFlow<ObserveScreenUiState> =
        MutableStateFlow(ObserveScreenUiState())
    val uiState: StateFlow<ObserveScreenUiState> = _uiState.asStateFlow()



    private val observerJobs = mutableMapOf<String, Job>()

    fun loadLovedOnes() {
        Log.i("Circle", "list: called")
        if (viewerId.isEmpty()) return

        viewModelScope.launch {


            when (val result =
                circleRepository.getAcceptedLovedOnes(viewerId)
            ) {
                is Resource.Success -> {
                    _uiState.update {

                        Log.i("Circle", "list: ${result.data}")
                        it.copy(
                            lovedOnes = result.data ?: emptyList(),
                        )
                    }
                }

                else -> {}
            }
        }
    }

    fun observeUsers() {


        val lovedOnes = _uiState.value.lovedOnes

        Log.i("OBSERVE", "user: ${lovedOnes}")

        lovedOnes.forEach {
            val userId = it.id


            if (observerJobs.contains(userId)) return

            val job = viewModelScope.launch {
                viewerLocationRepository.observeUserLocation(userId, viewerId)
                    .collect { viewerState ->
                        _uiState.update { state ->

                            Log.i("OBSERVE", "user: ${userId}")
                            state.copy(
                                locations = state.locations + (userId to viewerState)
                            )
                        }
                    }
            }

            observerJobs[userId] = job
        }
    }

    fun fetchViewerInfo() {
        viewModelScope.launch {
            val currentViewerIds = _uiState.value.locations.entries.filter { entry ->
                when (entry.value) {
                    is ViewerLocationState.NormalSharing -> true
                    is ViewerLocationState.EmergencySharing -> true
                    ViewerLocationState.Blocked -> false
                }
            }

            currentViewerIds.forEach {
                val user = googleSignInClient.findUserByUserId(it.key)

                if (user != null) {
                    if (!_uiState.value.currentViewers.contains(user)) {
                        _uiState.update {
                            it.copy(
                                currentViewers = it.currentViewers + user
                            )
                        }
                    }

                }
            }
        }
    }


    // job clean up when location sharing is not active
    fun cleanupRemovedUsers(activeUserIds: Set<String>) {
        observerJobs.keys
            .filter { it !in activeUserIds }
            .forEach { userId ->
                observerJobs.remove(userId)?.cancel()

                _uiState.update {
                    it.copy(
                        locations = it.locations - userId
                    )
                }
            }
    }

    // clear jobs when vm is destroyed
    override fun onCleared() {
        observerJobs.values.forEach { it.cancel() }
        observerJobs.clear()
        super.onCleared()
    }
}

data class ObserveScreenUiState(
    val lovedOnes: List<CircleMember> = emptyList(),
    val locations: Map<String, ViewerLocationState> = emptyMap(),
    val currentViewers: List<User> = emptyList(),
    val viewerState: ViewerLocationState? = ViewerLocationState.Blocked,
)