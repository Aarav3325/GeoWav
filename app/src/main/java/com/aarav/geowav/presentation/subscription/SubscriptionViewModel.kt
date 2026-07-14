package com.aarav.geowav.presentation.subscription

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.PurchaseResult
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.data.model.UserSubscription
import com.aarav.geowav.domain.repository.PaymentRepository
import com.aarav.geowav.domain.repository.SubscriptionRepository
import com.revenuecat.purchases.Package
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.aarav.geowav.data.model.PaywallConfig
import com.aarav.geowav.domain.repository.PaywallConfigRepository
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel
@Inject constructor(
    @ApplicationContext val context: Context,
    val subscriptionRepository: SubscriptionRepository,
    val paymentRepository: PaymentRepository,
    val googleSignInClient: GoogleSignInClient,
    private val paywallConfigRepository: PaywallConfigRepository
) : ViewModel() {

    val userPlan: StateFlow<UserPlan> =
        subscriptionRepository.observeUserPlan()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UserPlan.FREE
            )

    val paywallConfig: StateFlow<PaywallConfig> = paywallConfigRepository.paywallConfig
    private val _uiEvents = MutableSharedFlow<SubscriptionEvents>()
    val uiEvents = _uiEvents.asSharedFlow()


    private val _purchaseResult = MutableStateFlow<PurchaseResult?>(null)
    val purchaseResult: StateFlow<PurchaseResult?> = _purchaseResult.asStateFlow()


    private val _subscriptionState = MutableStateFlow<UserSubscription?>(null)
    val subscriptionState: StateFlow<UserSubscription?> = _subscriptionState.asStateFlow()

    private val _offeringState = MutableStateFlow(OfferingState())
    val offeringState: StateFlow<OfferingState> = _offeringState.asStateFlow()


    private var purchaseJob: Job? = null
    private var listeningJob: Job? = null

    init {
        Log.i("SUBSCRIPTION", "init")

        viewModelScope.launch {
            paywallConfigRepository.paywallConfig.collect { config ->
                Log.i("SUBSCRIPTION", "Fetching offerings dynamically for offering ID: ${config.offeringId}")
                fetchOfferings(config.offeringId)
            }
        }
//        startRealTimeEntitlementSync()
    }


    fun fetchOfferings(offeringId: String? = null) {

        Log.i("SUBSCRIPTION", "Offerings loading for offeringId: $offeringId")
        viewModelScope.launch {
            _offeringState.update { it.copy(isLoading = true, error = null) }
            when (val result = subscriptionRepository.fetchAllPackages(offeringId)) {
                is Resource.Success -> {
                    val allPackages = result.data.orEmpty()
                    _offeringState.update {
                        it.copy(
                            allPackages = allPackages,
                            isLoading = false,
                            error = if (allPackages.isEmpty()) "Offerings unavailable" else null
                        )
                    }
                    Log.i("SUBSCRIPTION", "Offerings loaded: ${allPackages}")
                }

                is Resource.NoInternet -> {
                    _offeringState.update {
                        it.copy(isLoading = false, error = "No internet connection")
                    }
                }

                is Resource.Timeout -> {
                    _offeringState.update {
                        it.copy(
                            isLoading = false,
                            error = "We're still loading plans. Please try again."
                        )
                    }
                }

                is Resource.ServerError -> {
                    _offeringState.update {
                        it.copy(isLoading = false, error = "We couldn't load plans right now.")
                    }
                }

                is Resource.UnknownError, is Resource.Error -> {
                    _offeringState.update {
                        it.copy(isLoading = false, error = result.message ?: "Offerings unavailable")
                    }
                }

                is Resource.Loading -> Unit
            }
        }
    }

    private fun startRealTimeEntitlementSync() {
        listeningJob?.cancel()
        listeningJob = viewModelScope.launch {
            paymentRepository.observeRealTimeEntitlements().collect { plan ->
                Log.i("SUBSCRIPTION", "Real-time plan updated: $plan")
            }
        }
    }

    fun purchasePlan(activity: Activity, plan: UserPlan) {
        val rcPackage = when (plan) {
            UserPlan.PREMIUM -> _offeringState.value.allPackages.find {
                it.identifier == "premium_monthly" || 
                it.identifier.contains("premium", ignoreCase = true) || 
                it.product.id.contains("premium", ignoreCase = true)
            }
            UserPlan.PRO -> _offeringState.value.allPackages.find {
                it.identifier == "pro_monthly" || 
                it.identifier.contains("pro", ignoreCase = true) || 
                it.product.id.contains("pro", ignoreCase = true)
            }
            UserPlan.FREE -> null
        }


        if (rcPackage == null) {
            viewModelScope.launch {
                _uiEvents.emit(SubscriptionEvents.ShowError("Package not available. Please try again."))
            }
            return
        }

        viewModelScope.launch {
            val result = paymentRepository.purchase(activity, rcPackage, plan)
            _purchaseResult.value = result
            when (result) {
                is PurchaseResult.Success ->
                    _uiEvents.emit(SubscriptionEvents.PurchaseSuccess(result))
                is PurchaseResult.Error ->
                    _uiEvents.emit(SubscriptionEvents.ShowError(result.message))
                PurchaseResult.Cancelled ->
                    _uiEvents.emit(SubscriptionEvents.PurchaseCancelled)
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            val result = paymentRepository.restorePurchases()
            _purchaseResult.value = result
            when (result) {
                is PurchaseResult.Success ->
                    _uiEvents.emit(SubscriptionEvents.PurchaseSuccess(result))
                is PurchaseResult.Error ->
                    _uiEvents.emit(SubscriptionEvents.ShowError(result.message))
                else -> Unit
            }
        }
    }


    fun fetchSubscriptionStatus() {
        fetchOfferings(paywallConfig.value.offeringId)
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
    object RestoreSuccess : SubscriptionEvents()
}

data class OfferingState(
    val allPackages: List<Package> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
