package com.aarav.geowav.domain.repository

import android.content.Context
import android.net.Uri
import com.aarav.geowav.data.model.PaymentTransactions
import com.aarav.geowav.data.model.UpiApp

interface PaymentRepository {

    fun createUpiUri(
        upiId: String,
        name: String,
        amount: String,
        note: String
    ): Uri

    fun getUpiApps(
        context: Context,
        uri: Uri
    ): List<UpiApp>

    fun parseUpiResponse(
        response: String?
    ): Map<String, String>

    fun savePayment(
        payment: PaymentTransactions
    )
}