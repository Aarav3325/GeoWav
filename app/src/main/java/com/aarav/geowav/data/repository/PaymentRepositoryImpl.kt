package com.aarav.geowav.data.repository

import android.app.Activity
import android.util.Log
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.datasource.revenuecat.RevenueCatDataSource
import com.aarav.geowav.data.model.PurchaseResult
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.data.model.getPlanDuration
import com.aarav.geowav.domain.repository.PaymentRepository
import com.google.firebase.database.FirebaseDatabase
import com.revenuecat.purchases.Package
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val revenueCatDataSource: RevenueCatDataSource,
    private val firebaseDatabase: FirebaseDatabase,
    private val googleSignInClient: GoogleSignInClient
) : PaymentRepository {

    private val TAG = "PaymentRepository"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _purchaseEvents = MutableSharedFlow<PurchaseResult>()

    init {
        scope.launch {
            revenueCatDataSource.onEntitlementChanged.collectLatest {
                Log.d(TAG, "Global sync triggered by RevenueCat change")
                syncEntitlements()
            }
        }
        
//        scope.launch {
//            syncEntitlements()
//        }
    }

    override fun observePurchasesUpdate(): Flow<PurchaseResult> =
        _purchaseEvents.asSharedFlow()

    override suspend fun purchase(
        activity: Activity,
        rcPackage: Package,
        plan: UserPlan
    ): PurchaseResult {
        Log.i(TAG, "Initiating purchase for ${plan.name}")
        val result = revenueCatDataSource.purchase(activity, rcPackage, plan)

        if (result is PurchaseResult.Success) {
            val expiryTime = result.purchaseTime + getPlanDuration(plan)
            savePurchase(
                plan = plan.name,
                token = result.purchaseToken,
                purchaseTime = result.purchaseTime,
                expiryTime = expiryTime,
                isAutoRenewing = true,
                active = true
            )
        }

        _purchaseEvents.emit(result)
        return result
    }

    override fun savePurchase(
        plan: String,
        token: String,
        purchaseTime: Long,
        expiryTime: Long,
        isAutoRenewing: Boolean,
        active: Boolean
    ) {
        val uid = googleSignInClient.getUserId()
        if (uid.isBlank()) return

        val ref = firebaseDatabase
            .getReference("subscriptions")
            .child(uid)

        val updates = hashMapOf<String, Any>(
            "plan" to plan,
            "active" to active,
            "source" to "REVENUECAT",
            "purchaseToken" to token,
            "purchaseTime" to purchaseTime,
            "updatedAt" to System.currentTimeMillis(),
            "expiryTime" to expiryTime,
            "autoRenewing" to isAutoRenewing
        )

        ref.updateChildren(updates)
        Log.i(TAG, "Subscription updated in Firebase: $plan (Expiry: $expiryTime)")
    }

    override suspend fun initializeUser(uid: String) {
        if (uid.isBlank()) {
            revenueCatDataSource.reset()
            return
        }
        
        // This will trigger the identify call which then emits the sync signal
        revenueCatDataSource.identify(uid)
    }

    override suspend fun clear() {
        revenueCatDataSource.reset()
    }

    override suspend fun restorePurchases(): PurchaseResult {
        Log.i(TAG, "Restoring purchases...")
        val customerInfo = revenueCatDataSource.restorePurchases()
            ?: return PurchaseResult.Error("Restore failed")

        val plan = revenueCatDataSource.resolveActivePlan(customerInfo)
        val expiryTime = revenueCatDataSource.getExpiryTime(customerInfo)
        val purchaseTime = revenueCatDataSource.getPurchaseTime(customerInfo)

        return if (plan != UserPlan.FREE) {
            savePurchase(
                plan = plan.name,
                token = "restored",
                purchaseTime = purchaseTime,
                expiryTime = expiryTime,
                isAutoRenewing = true,
                active = true
            )
            
            PurchaseResult.Success(
                plan = plan,
                purchaseToken = "restored",
                orderId = null,
                purchaseTime = purchaseTime
            )
        } else {
            savePurchase("FREE", "", 0L, 0L, false, false)
            PurchaseResult.Error("No active subscription found")
        }
    }

    override suspend fun syncEntitlements(): UserPlan {
        Log.i(TAG, "Syncing entitlements with cache invalidation...")
        val customerInfo = revenueCatDataSource.getCustomerInfo(forceRefresh = true)
            ?: return UserPlan.FREE
        Log.i(TAG, "Customer info on sync: ${com.revenuecat.purchases.Purchases.sharedInstance.appUserID}")

        val plan = revenueCatDataSource.resolveActivePlan(customerInfo)
        val expiryTime = revenueCatDataSource.getExpiryTime(customerInfo)
        val purchaseTime = revenueCatDataSource.getPurchaseTime(customerInfo)

        savePurchase(
            plan = plan.name,
            token = "",
            purchaseTime = purchaseTime,
            expiryTime = expiryTime,
            isAutoRenewing = plan != UserPlan.FREE,
            active = plan != UserPlan.FREE
        )

        // Schedule a proactive sync if there's an active subscription about to expire
        if (plan != UserPlan.FREE && expiryTime > System.currentTimeMillis()) {
            scheduleExpirySync(expiryTime)
        }

        Log.i(TAG, "Entitlements synced. Active plan: $plan")
        return plan
    }

    private fun scheduleExpirySync(expiryTime: Long) {
        val delayMs = (expiryTime - System.currentTimeMillis()).coerceAtLeast(0L)
        Log.d(TAG, "Scheduling proactive expiry sync in ${delayMs / 1000} seconds")
        scope.launch {
            delay(delayMs + 2000) // Buffer 2 seconds to ensure RevenueCat server is updated
            Log.d(TAG, "Executing proactive expiry sync now that time has passed")
            syncEntitlements()
        }
    }

    override fun observeRealTimeEntitlements(): Flow<UserPlan> =
        revenueCatDataSource.onEntitlementChanged
            .map {
                // When RevenueCat notifies of a change, we force a fresh sync
                syncEntitlements()
            }
}