package com.aarav.geowav.data.repository

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import com.aarav.geowav.data.model.PaymentTransactions
import com.aarav.geowav.data.model.UpiApp
import com.aarav.geowav.domain.repository.PaymentRepository
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    val firebaseDatabase: FirebaseDatabase
) : PaymentRepository {

    private val BILLING_TAG = "BILLING"

    lateinit var purchasesUpdatedListener: PurchasesUpdatedListener
    lateinit var billingClient: BillingClient

    lateinit var pendingPurchase: Purchase

    override fun createUpiUri(
        upiId: String,
        name: String,
        amount: String,
        note: String
    ): Uri {

        return Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", name)
            .appendQueryParameter("tn", note)
            .appendQueryParameter("am", amount)
            .appendQueryParameter("cu", "INR")
            .build()
    }

    override fun getUpiApps(
        context: Context,
        uri: Uri
    ): List<UpiApp> {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
        }

        val pm = context.packageManager

        val resolveInfo = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        Log.i("UPI", "apps:" + resolveInfo.size.toString())

        return resolveInfo.map {
            UpiApp(
                name = it.loadLabel(pm).toString(),
                packageName = it.activityInfo.packageName,
                icon = it.loadIcon(pm)
            )
        }
    }

    override fun parseUpiResponse(response: String?): Map<String, String> {
        val result = mutableMapOf<String, String>()

        response?.split("&")?.forEach {
            val pair = it.split("=")

            if (pair.size >= 2) {
                result[pair[0]] = pair[1]
            }
        }

        return result
    }

    override fun savePayment(payment: PaymentTransactions) {
        val ref = firebaseDatabase.getReference("payments")
            .child(payment.userId)

        ref.push()
            .setValue(payment)
    }

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
        purchasesUpdatedListener = PurchasesUpdatedListener { p0, p1 ->
            if (p0.responseCode == BillingClient.BillingResponseCode.OK && p1 != null) {
                for (purchase in p1) {
                    Log.i(BILLING_TAG, "purchase success: ${purchase.orderId}")
                }
            } else if (p0.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                Log.i(BILLING_TAG, "purchase cancelled")
            } else {
                Log.i(BILLING_TAG, "purchase failed: ${p0.responseCode}")
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

    override suspend fun processPurchases(
        activity: Activity
    ) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("test_subscription_1")
                .setProductType(BillingClient.ProductType.SUBS)
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