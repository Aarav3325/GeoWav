package com.aarav.geowav.data.datasource.revenuecat

import android.app.Activity
import android.util.Log
import com.aarav.geowav.data.model.PurchaseResult
import com.aarav.geowav.data.model.UserPlan
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevenueCatDataSource @Inject constructor() {
    private val TAG = "RevenueCat"


    private val _purchaseEvents = MutableSharedFlow<PurchaseResult>()
    val purchaseEvents= _purchaseEvents.asSharedFlow()

    suspend fun fetchOfferings(): Offerings? {
        return try {
            Purchases.sharedInstance.awaitOfferings()
        }
        catch (e: Exception) {
            Log.e(TAG, "Failed to fetch offerings: ${e.message}")
            null
        }
    }

    suspend fun getAllPackages(): List<Package>? {
        val offerings = fetchOfferings() ?: return null
        return offerings.current?.availablePackages
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

    suspend fun getCustomerInfo(): CustomerInfo? {
        return try {
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
}