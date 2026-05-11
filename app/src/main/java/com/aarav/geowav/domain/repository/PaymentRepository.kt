package com.aarav.geowav.domain.repository

import android.app.Activity
import android.content.Context
import android.net.Uri
import com.aarav.geowav.data.model.PaymentTransactions
import com.aarav.geowav.data.model.PurchaseResult
import com.aarav.geowav.data.model.UpiApp
import com.aarav.geowav.data.model.UserPlan
import com.android.billingclient.api.Purchase
import com.revenuecat.purchases.Package
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {

    suspend fun createBillingClient(
        context: Context
    )

    fun observePurchasesUpdate(): Flow<PurchaseResult>

    suspend fun processPurchases(
        activity: Activity,
        productId: String
    )

    fun savePurchase(
        plan: String,
        token:  String,
        purchaseTime: Long,
        expiryTime: Long,
        isAutoRenewing: Boolean
    )

    suspend fun syncAfterLogin(context: Context)

    fun syncPurchases()

    suspend fun purchase(activity: Activity, rcPackage: Package, plan: UserPlan): PurchaseResult

    suspend fun restorePurchases(): PurchaseResult

    suspend fun syncEntitlements(): UserPlan

}