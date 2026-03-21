package com.aarav.geowav.presentation.subscription

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.PurchaseResult
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.data.model.UserSubscription
import com.aarav.geowav.domain.repository.PaymentRepository
import com.aarav.geowav.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel
@Inject constructor(
    @ApplicationContext val context: Context,
    val subscriptionRepository: SubscriptionRepository,
    val paymentRepository: PaymentRepository,
    val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    val userPlan: StateFlow<UserPlan> =
        subscriptionRepository.observeUserPlan()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UserPlan.FREE
            )
    private val _uiEvents = MutableSharedFlow<SubscriptionEvents>()
    val uiEvents = _uiEvents.asSharedFlow()


    private val _purchaseResult = MutableStateFlow<PurchaseResult?>(null)
    val purchaseResult: StateFlow<PurchaseResult?> = _purchaseResult.asStateFlow()


    private val _subscriptionState = MutableStateFlow<UserSubscription?>(null)
    val subscriptionState: StateFlow<UserSubscription?> = _subscriptionState.asStateFlow()


    private var purchaseJob: Job? = null
    private var listeningJob: Job? = null

    init {
        Log.i("SUBSCRIPTION", "init")

        setupBillingClient()
        observePurchases()
    }

//    fun startListening() {
//        listeningJob?.cancel()
//        listeningJob = viewModelScope.launch {
//            subscriptionRepository.observeUserPlan().collect {
//                _userPlan.value = it
//            }
//        }
//    }

    fun setupBillingClient() {
        viewModelScope.launch {
            paymentRepository.createBillingClient(context)
        }
    }

    fun launchBillingFlow(
        activity: Activity,
        productId: String
    ) {
        viewModelScope.launch {
            paymentRepository.processPurchases(activity, productId)
        }
    }

    fun observePurchases() {

        purchaseJob?.cancel()

        purchaseJob = viewModelScope.launch {
            paymentRepository.observePurchasesUpdate()
                .collect { purchaseResult ->
                    when (purchaseResult) {
                        is PurchaseResult.Success -> {

                            _purchaseResult.value = purchaseResult
                            _uiEvents.emit(
                                SubscriptionEvents.PurchaseSuccess(purchaseResult)
                            )
                        }

                        is PurchaseResult.Error -> {

                            _purchaseResult.value = purchaseResult
                            _uiEvents.emit(
                                SubscriptionEvents.ShowError(purchaseResult.message)
                            )
                        }

                        PurchaseResult.Cancelled -> {

                            _purchaseResult.value = purchaseResult
                            _uiEvents.emit(
                                SubscriptionEvents.PurchaseCancelled
                            )
                        }
                    }
                }
        }
    }

    fun fetchSubscriptionStatus() {
        viewModelScope.launch {
            subscriptionRepository.fetchSubscriptionStatus()
                .collect {
                    _subscriptionState.value = it
                }
        }
    }

    fun clearPurchaseResult() {
        _purchaseResult.value = null
    }

    override fun onCleared() {
        super.onCleared()
        purchaseJob?.cancel()
        listeningJob?.cancel()
    }

}

sealed class SubscriptionEvents {
    data class PurchaseSuccess(val purchaseSuccess: PurchaseResult.Success) : SubscriptionEvents()
    object PurchaseCancelled : SubscriptionEvents()
    data class ShowError(val message: String) : SubscriptionEvents()
}