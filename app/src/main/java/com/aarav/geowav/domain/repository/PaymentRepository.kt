package com.aarav.geowav.domain.repository

import android.app.Activity
import com.aarav.geowav.data.model.PurchaseResult
import com.aarav.geowav.data.model.UserPlan
import com.revenuecat.purchases.Package
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {

    fun observePurchasesUpdate(): Flow<PurchaseResult>

    fun savePurchase(
        plan: String,
        token: String,
        purchaseTime: Long,
        expiryTime: Long,
        isAutoRenewing: Boolean
    )

    suspend fun purchase(activity: Activity, rcPackage: Package, plan: UserPlan): PurchaseResult

    suspend fun restorePurchases(): PurchaseResult

    suspend fun syncEntitlements(): UserPlan
}