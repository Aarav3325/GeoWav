package com.aarav.geowav.presentation.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel
@Inject constructor(
    val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _userPlan = MutableStateFlow(UserPlan.FREE)
    val userPlan: StateFlow<UserPlan> = _userPlan.asStateFlow()

    fun startListening() {
        viewModelScope.launch {
            subscriptionRepository.observeUserPlan().collect {
                _userPlan.value = it
            }
        }
    }

}