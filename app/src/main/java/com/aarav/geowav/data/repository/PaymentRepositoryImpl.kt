package com.aarav.geowav.data.repository

import android.app.Activity
import android.util.Log
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.datasource.revenuecat.RevenueCatDataSource
import com.aarav.geowav.data.model.PurchaseResult
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.data.model.UserSubscription
import com.aarav.geowav.data.model.getPlanDuration
import com.aarav.geowav.domain.repository.PaymentRepository
import com.google.firebase.database.FirebaseDatabase
import com.revenuecat.purchases.Package
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val revenueCatDataSource: RevenueCatDataSource,
    private val firebaseDatabase: FirebaseDatabase,
    private val googleSignInClient: GoogleSignInClient
) : PaymentRepository {

    private val TAG = "PaymentRepository"
    private val _purchaseEvents = MutableSharedFlow<PurchaseResult>()

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
        Log.i(TAG, "Subscription updated in Firebase: $plan")
    }

    override suspend fun restorePurchases(): PurchaseResult {
        Log.i(TAG, "Restoring purchases...")
        val customerInfo = revenueCatDataSource.restorePurchases()
            ?: return PurchaseResult.Error("Restore failed")

        val plan = revenueCatDataSource.resolveActivePlan(customerInfo)

        return if (plan != UserPlan.FREE) {
            savePurchase(
                plan = plan.name,
                token = "restored",
                purchaseTime = System.currentTimeMillis(),
                expiryTime = System.currentTimeMillis() + getPlanDuration(plan),
                isAutoRenewing = true,
                active = true
            )
            PurchaseResult.Success(
                plan = plan,
                purchaseToken = "restored",
                orderId = null,
                purchaseTime = System.currentTimeMillis()
            )
        } else {
            savePurchase("FREE", "", 0L, 0L, false, false)
            PurchaseResult.Error("No active subscription found")
        }
    }

    override suspend fun syncEntitlements(): UserPlan {
        Log.i(TAG, "Syncing entitlements...")
        val customerInfo = revenueCatDataSource.getCustomerInfo() ?: return UserPlan.FREE
        val plan = revenueCatDataSource.resolveActivePlan(customerInfo)

        savePurchase(
            plan = plan.name,
            token = "",
            purchaseTime = 0L,
            expiryTime = 0L,
            isAutoRenewing = plan != UserPlan.FREE,
            active = plan != UserPlan.FREE
        )

        Log.i(TAG, "Entitlements synced. Active plan: $plan")
        return plan
    }
}