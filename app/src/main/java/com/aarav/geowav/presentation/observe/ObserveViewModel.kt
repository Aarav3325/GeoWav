package com.aarav.geowav.presentation.observe

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.domain.repository.CircleRepository
import com.aarav.geowav.presentation.home.HomeScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ObserveViewModel
@Inject constructor(
    val circleRepository: CircleRepository,
    val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    val viewerId = googleSignInClient.getUserId()

    private val _uiState: MutableStateFlow<ObserveScreenUiState> =
        MutableStateFlow(ObserveScreenUiState())
    val uiState: StateFlow<ObserveScreenUiState> = _uiState.asStateFlow()


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

}

data class ObserveScreenUiState(
    val lovedOnes: List<CircleMember> = emptyList(),
)