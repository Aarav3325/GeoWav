package com.aarav.geowav.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.PurchaseResult
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.data.model.UserSubscription
import com.aarav.geowav.data.model.getPlanDuration
import com.aarav.geowav.domain.repository.PaymentRepository
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    val firebaseDatabase: FirebaseDatabase,
    val googleSignInClient: GoogleSignInClient
) : PaymentRepository {

    private var isBillingInitialized = false

    private val BILLING_TAG = "BILLING"
    private val _purchaseEvents = MutableSharedFlow<PurchaseResult>()
    val purchaseEvents = _purchaseEvents.asSharedFlow()


    private var purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                Log.i(BILLING_TAG, "purchase success: ${purchase.orderId}")
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (!purchase.isAcknowledged) {
                        val params = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()

                        billingClient.acknowledgePurchase(params) {
                            Log.i(BILLING_TAG, "Purchase acknowledged")
                        }
                    }

                    val productId = purchase.products.first()

                    val plan = when (productId) {
                        "geowav_premium" -> "PREMIUM"
                        "geowav_pro" -> "PRO"
                        else -> "PREMIUM"
                    }

                    val finalPurchaseTime = if (
                        purchase.purchaseTime == 0L
                    ) System.currentTimeMillis() else purchase.purchaseTime

                    val expiryTime = finalPurchaseTime + getPlanDuration(UserPlan.valueOf(plan))

                    savePurchase(
                        plan,
                        purchase.purchaseToken,
                        finalPurchaseTime,
                        expiryTime,
                        purchase.isAutoRenewing
                    )

                    Log.i(BILLING_TAG, "Plan detected: $plan")

                    CoroutineScope(Dispatchers.IO).launch {
                        _purchaseEvents.emit(
                            PurchaseResult.Success(
                                plan = UserPlan.valueOf(plan),
                                purchaseToken = purchase.purchaseToken,
                                orderId = purchase.orderId,
                                purchaseTime = finalPurchaseTime
                            )
                        )
                    }
                }
            }
        } else if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i(BILLING_TAG, "purchase cancelled")
            CoroutineScope(Dispatchers.IO).launch {
                _purchaseEvents.emit(
                    PurchaseResult.Cancelled
                )
            }
        } else {
            Log.i(BILLING_TAG, "purchase failed: ${result.responseCode}")
            CoroutineScope(Dispatchers.IO).launch {
                _purchaseEvents.emit(
                    PurchaseResult.Error(
                        result.debugMessage
                    )
                )
            }
        }
    }
    lateinit var billingClient: BillingClient

    override suspend fun createBillingClient(context: Context) {
        billingClient = BillingClient.newBuilder(
            context
        )
            .setListener(purchasesUpdatedListener).enablePendingPurchases(
                PendingPurchasesParams
                    .newBuilder()
                    .enablePrepaidPlans()
                    .enableOneTimeProducts()
                    .build()
            )
            .enableAutoServiceReconnection()
            .build()

        isBillingInitialized = true

        connectToGooglePlay()
    }

    override fun observePurchasesUpdate(): Flow<PurchaseResult> {
        return purchaseEvents
    }

//    override fun observePurchasesUpdate(): Flow<PurchaseResult> = callbackFlow {
//        purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
//            if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
//                for (purchase in purchases) {
//                    Log.i(BILLING_TAG, "purchase success: ${purchase.orderId}")
//                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
//                        if (!purchase.isAcknowledged) {
//                            val params = AcknowledgePurchaseParams.newBuilder()
//                                .setPurchaseToken(purchase.purchaseToken)
//                                .build()
//
//                            billingClient.acknowledgePurchase(params) {
//                                Log.i(BILLING_TAG, "Purchase acknowledged")
//                            }
//                        }
//
//                        val productId = purchase.products.first()
//
//                        val plan = when (productId) {
//                            "geowav_premium" -> "PREMIUM"
//                            "geowav_pro" -> "PRO"
//                            else -> "PREMIUM"
//                        }
//
//                        savePurchase(
//                            plan,
//                            purchase.purchaseToken,
//                            purchase.purchaseTime
//                        )
//
//                        Log.i(BILLING_TAG, "Plan detected: $plan")
//
//                        trySend(
//                            PurchaseResult.Success(
//                                plan = UserPlan.valueOf(plan),
//                                purchaseToken = purchase.purchaseToken,
//                                orderId = purchase.orderId,
//                                purchaseTime = purchase.purchaseTime
//                            )
//                        )
//                    }
//                }
//            } else if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
//                Log.i(BILLING_TAG, "purchase cancelled")
//                trySend(PurchaseResult.Cancelled)
//            } else {
//                Log.i(BILLING_TAG, "purchase failed: ${result.responseCode}")
//                trySend(
//                    PurchaseResult.Error(
//                        result.debugMessage
//                    )
//                )
//            }
//        }
//
//        awaitClose {
//            Log.i(BILLING_TAG, "Purchase listener removed")
//        }
//    }


    fun connectToGooglePlay() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {

            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                if (p0.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(BILLING_TAG, "Billing client connected")
                    syncPurchases()
                }
            }

        })
    }

    override fun savePurchase(
        plan: String,
        token: String,
        purchaseTime: Long,
        expiryTime: Long,
        isAutoRenewing: Boolean
    ) {
        val uid = googleSignInClient.getUserId()

        if (uid.isBlank()) {
            return
        }

        val ref = firebaseDatabase
            .getReference("subscriptions")
            .child(uid)

        val subscriptionData = UserSubscription(
            plan = plan,
            isActive = true,
            purchaseToken = token,
            updatedAt = System.currentTimeMillis(),
            expiryTime = expiryTime,
            isAutoRenewing = isAutoRenewing
        )

//        val data = mapOf(
//            "plan" to plan,
//            "isActive" to true,
//            "purchaseToken" to token,
//            "purchaseTime" to purchaseTime,
//            "updatedAt" to System.currentTimeMillis()
//        )

        ref.setValue(subscriptionData)
    }

    override suspend fun syncAfterLogin(context: Context) {
        if (!isBillingInitialized) {
            createBillingClient(context)
        } else {
            connectToGooglePlay()
        }
    }

    override fun syncPurchases() {
        val uid = googleSignInClient.getUserId()
        if (uid.isBlank()) return

        if (uid == "7sZTZoNLRpUBcJSevQJyNq2XRVw1") return

        Log.i(BILLING_TAG, "syncPurchases called")

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                if (purchases.isEmpty()) {
                    savePurchase(
                        "FREE",
                        "",
                        purchaseTime = 0L,
                        expiryTime = 0L,
                        isAutoRenewing = false
                    )

                    Log.i(BILLING_TAG, "No active subscription")
                } else {
                    Log.i(BILLING_TAG, purchases.toString())
                    var finalPlan = "FREE"
                    var token = ""
                    var purchaseTime = 0L
                    var expiryTime = 0L
                    var isAutoRenewing = false

                    for (purchase in purchases) {

                        val productId = purchase.products.first()

                        if (productId == "geowav_pro") {
                            finalPlan = "PRO"
                            token = purchase.purchaseToken
                            purchaseTime = purchase.purchaseTime
                            expiryTime = purchase.purchaseTime + getPlanDuration(UserPlan.valueOf(finalPlan))
                            isAutoRenewing = purchase.isAutoRenewing
                            break
                        } else if (productId == "geowav_premium") {
                            finalPlan = "PREMIUM"
                            token = purchase.purchaseToken
                            purchaseTime = purchase.purchaseTime
                            expiryTime = purchase.purchaseTime + getPlanDuration(UserPlan.valueOf(finalPlan))
                            isAutoRenewing = purchase.isAutoRenewing
                        }
                    }

                    savePurchase(
                        plan = finalPlan,
                        token = token,
                        purchaseTime = purchaseTime,
                        expiryTime = expiryTime,
                        isAutoRenewing = isAutoRenewing
                    )

                    Log.i(BILLING_TAG, "Synced plan: $finalPlan")

                }
            }
        }
    }

    override suspend fun processPurchases(
        activity: Activity,
        productId: String
    ) {
//        val productList = listOf(
//            QueryProductDetailsParams.Product.newBuilder()
//                .setProductId(productId)
//                .setProductType(BillingClient.ProductType.SUBS)
//                .build()
//        )

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("android.test.purchased")
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params.build())
        }


        val productDetailsList = productDetailsResult.productDetailsList

        if (productDetailsList.isNullOrEmpty()) {
            Log.e(BILLING_TAG, "No products found")
            return
        }

        val productDetails = productDetailsList.first()

        Log.d("BILLING", "Offer details: ${productDetails.subscriptionOfferDetails}")

//        val offerToken = productDetails.subscriptionOfferDetails
//            ?.firstOrNull()
//            ?.offerToken
//
//        if (offerToken == null) {
//            Log.e(BILLING_TAG, "Offer token not found")
//            return
//        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                //.setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val result = billingClient.launchBillingFlow(
            activity,
            billingFlowParams
        )

        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.i(
                BILLING_TAG,
                "process purchase:" + productDetailsResult.productDetailsList.toString()
            )
        }
    }
}