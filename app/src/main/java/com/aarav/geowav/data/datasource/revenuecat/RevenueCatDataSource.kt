package com.aarav.geowav.data.datasource.revenuecat

import android.app.Activity
import android.util.Log
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.core.utils.withNetworkTimeout
import com.aarav.geowav.data.model.PurchaseResult
import com.aarav.geowav.data.model.UserPlan
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesErrorCode
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitLogIn
import com.revenuecat.purchases.awaitLogOut
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevenueCatDataSource @Inject constructor() {
    private val TAG = "RevenueCat"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _onEntitlementChanged = MutableSharedFlow<Unit>(replay = 0)
    val onEntitlementChanged: SharedFlow<Unit> = _onEntitlementChanged.asSharedFlow()

    val _isInitialized = MutableStateFlow(false)
    val isInitialized = _isInitialized.asSharedFlow()

    init {
        Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener {
            Log.i(TAG, "Customer info update detected by RevenueCat listener")
            scope.launch {
                _onEntitlementChanged.emit(Unit)
            }
        }
    }

    suspend fun identify(uid: String): Boolean {
        return try {
            Log.i(TAG, "Identifying user in RC: $uid")
            Purchases.sharedInstance.awaitLogIn(uid)
            _isInitialized.value = true
            
            // Trigger the sync flow now that we are identified
            _onEntitlementChanged.emit(Unit)
            
            Log.i(TAG, "Identify success for $uid")
            true
        }
        catch (e: Exception) {
            _isInitialized.value = false
            Log.e(TAG, "Identify failed: ${e.message}")
            false
        }
    }

    fun isInitialized(): Boolean = _isInitialized.value

    suspend fun reset() {
        try {
            if (!Purchases.sharedInstance.isAnonymous) {
                Log.i(TAG, "Logging out RevenueCat user")
                Purchases.sharedInstance.awaitLogOut()
            }
            _isInitialized.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Error during RevenueCat logout: ${e.message}")
        }
    }

    suspend fun fetchAllPackages(): Resource<List<Package>> {
        if(!isInitialized()) return Resource.UnknownError("Offerings unavailable")
        Log.i(TAG, "Offerings loading...")
        return withNetworkTimeout {
            val offerings = Purchases.sharedInstance.awaitOfferings()
            offerings.current?.availablePackages.orEmpty()
        }
    }

    suspend fun purchase(
        activity: Activity,
        rcPackage: Package,
        plan: UserPlan
    ): PurchaseResult {
        if(!isInitialized()) return PurchaseResult.Error("RevenueCat not initialized")
        return try {
            val result = Purchases.sharedInstance.awaitPurchase(
                PurchaseParams.Builder(
                    activity,
                    rcPackage
                )
                    .build()
            )

            Log.i(TAG, "Purchase success: ${result.customerInfo.activeSubscriptions}")

            PurchaseResult.Success(
                plan = plan,
                purchaseToken = result.storeTransaction.purchaseToken ?: "",
                orderId = result.storeTransaction.orderId,
                purchaseTime = result.storeTransaction.purchaseTime ?: System.currentTimeMillis(),
                expiryTime = getExpiryTime(result.customerInfo)
            )
        }
        catch (e: com.revenuecat.purchases.PurchasesException) {
            if (e.error.code == com.revenuecat.purchases.PurchasesErrorCode.PurchaseCancelledError) {
                Log.i(TAG, "Purchase cancelled by user")
                PurchaseResult.Cancelled
            } else {
                Log.e(TAG, "Purchase error: ${e.error.message}")
                PurchaseResult.Error(e.error.message ?: "Purchase failed")
            }
        }
    }

    suspend fun restorePurchases(): CustomerInfo? {
        if(!isInitialized()) return null
        return try {
            Purchases.sharedInstance.awaitRestore()
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed: ${e.message}")
            null
        }
    }

    suspend fun getCustomerInfo(forceRefresh: Boolean = false): CustomerInfo? {
        if(!isInitialized()) return null
        return try {
            if (forceRefresh) {
                Log.d(TAG, "Invalidating CustomerInfo cache to fetch fresh data")
                Purchases.sharedInstance.invalidateCustomerInfoCache()
            }
            Purchases.sharedInstance.awaitCustomerInfo()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get customer info: ${e.message}")
            null
        }
    }

    fun resolveActivePlan(customerInfo: CustomerInfo): UserPlan {
        if(!isInitialized()) return UserPlan.FREE

        val entitlements = customerInfo.entitlements.active
        return when {
            entitlements.containsKey("pro") -> UserPlan.PRO
            entitlements.containsKey("premium") -> UserPlan.PREMIUM
            else -> UserPlan.FREE
        }
    }

    fun getExpiryTime(customerInfo: CustomerInfo): Long {
        val entitlements = customerInfo.entitlements.active
        val activeEntitlement = entitlements["pro"] ?: entitlements["premium"]
        return activeEntitlement?.expirationDate?.time ?: 0L
    }

    fun getPurchaseTime(customerInfo: CustomerInfo): Long {
        val entitlements = customerInfo.entitlements.active
        val activeEntitlement = entitlements["pro"] ?: entitlements["premium"]
        return activeEntitlement?.latestPurchaseDate?.time ?: System.currentTimeMillis()
    }
}
