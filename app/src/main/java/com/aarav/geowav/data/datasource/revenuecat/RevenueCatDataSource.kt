package com.aarav.geowav.data.datasource.revenuecat

import android.app.Activity
import android.util.Log
import com.aarav.geowav.data.model.PurchaseResult
import com.aarav.geowav.data.model.UserPlan
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesErrorCode
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevenueCatDataSource @Inject constructor() {
    private val TAG = "RevenueCat"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _onEntitlementChanged = MutableSharedFlow<Unit>(replay = 1)
    val onEntitlementChanged: SharedFlow<Unit> = _onEntitlementChanged.asSharedFlow()

    init {
        Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener {
            Log.i(TAG, "Customer info update detected by RevenueCat listener")
            scope.launch {
                _onEntitlementChanged.emit(Unit)
            }
        }
        
        scope.launch {
            _onEntitlementChanged.emit(Unit)
        }
    }

    suspend fun fetchAllPackages(): List<Package>? {
        Log.i(TAG, "Offerings loading...")
        return try {
            val offerings = Purchases.sharedInstance.awaitOfferings()
            offerings.current?.availablePackages
        } catch (e: Exception) {
            Log.e(TAG, "Offerings error: ${e.message}")
            null
        }
    }

    suspend fun purchase(
        activity: Activity,
        rcPackage: Package,
        plan: UserPlan
    ): PurchaseResult {
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
                purchaseTime = result.storeTransaction.purchaseTime ?: System.currentTimeMillis()
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
        return try {
            Purchases.sharedInstance.awaitRestore()
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed: ${e.message}")
            null
        }
    }

    suspend fun getCustomerInfo(forceRefresh: Boolean = false): CustomerInfo? {
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