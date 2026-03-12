package com.aarav.geowav.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import com.aarav.geowav.data.model.PaymentTransactions
import com.aarav.geowav.data.model.UpiApp
import com.aarav.geowav.domain.repository.PaymentRepository
import androidx.core.net.toUri
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    val firebaseDatabase: FirebaseDatabase
): PaymentRepository {

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

            if(pair.size >= 2) {
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
}