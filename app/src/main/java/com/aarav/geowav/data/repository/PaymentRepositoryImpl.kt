package com.aarav.geowav.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.aarav.geowav.data.authentication.GoogleSignInClient
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    val firebaseDatabase: FirebaseDatabase,
    val googleSignInClient: GoogleSignInClient
) : PaymentRepository {

    private val BILLING_TAG = "BILLING"

    lateinit var purchasesUpdatedListener: PurchasesUpdatedListener
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

        connectToGooglePlay()
    }

    override fun observePurchasesUpdate() {
        purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
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
                            else -> "FREE"
                        }

                        savePurchase(
                            plan,
                            purchase.purchaseToken,
                            purchase.purchaseTime
                        )

                        Log.i(BILLING_TAG, "Plan detected: $plan")
                    }
                }
            } else if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                Log.i(BILLING_TAG, "purchase cancelled")
            } else {
                Log.i(BILLING_TAG, "purchase failed: ${result.responseCode}")
            }
        }
    }


    fun connectToGooglePlay() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {

            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                if (p0.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(BILLING_TAG, "Billing client connected")
                }
            }

        })
    }

    override fun savePurchase(
        plan: String,
        token: String,
        purchaseTime: Long
    ) {
        val uid = googleSignInClient.getUserId()

        if (uid.isBlank()) {
            return
        }

        val ref = firebaseDatabase
            .getReference("subscriptions")
            .child(uid)


        val data = mapOf(
            "plan" to plan,
            "isActive" to true,
            "purchaseToken" to token,
            "purchaseTime" to purchaseTime,
            "updatedAt" to System.currentTimeMillis()
        )

        ref.setValue(data)
    }

    override fun syncPurchases() {
        val uid = googleSignInClient.getUserId()
        if (uid.isBlank()) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                if (purchases.isEmpty()) {
                    savePurchase(
                        "FREE",
                        "",
                        purchaseTime = 0L
                    )

                    Log.i(BILLING_TAG, "No active subscription")
                } else {
                    Log.i(BILLING_TAG, purchases.toString())
                    var finalPlan = "FREE"
                    var token = ""
                    var purchaseTime = 0L

                    for (purchase in purchases) {

                        val productId = purchase.products.first()

                        if (productId == "geowav_pro") {
                            finalPlan = "PRO"
                            token = purchase.purchaseToken
                            purchaseTime = purchase.purchaseTime
                            break
                        } else if (productId == "geowav_premium") {
                            finalPlan = "PREMIUM"
                            token = purchase.purchaseToken
                            purchaseTime = purchase.purchaseTime
                        }
                    }

                    savePurchase(
                        plan = finalPlan,
                        token = token,
                        purchaseTime = purchaseTime
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
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

//        val productList = listOf(
//            QueryProductDetailsParams.Product.newBuilder()
//                .setProductId("android.test.purchased")
//                .setProductType(BillingClient.ProductType.INAPP)
//                .build()
//        )

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

        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull()
            ?.offerToken

        if (offerToken == null) {
            Log.e(BILLING_TAG, "Offer token not found")
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
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