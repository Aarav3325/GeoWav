package com.aarav.geowav.data.model

import android.graphics.drawable.Drawable

data class PaymentTransactions(
    val orderId: String = "",
    val amount: Double = 0.0,
    val status: String = "",
    val txnId: String = "",
    val upiRef: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class UpiApp(
    val name: String,
    val packageName: String,
    val icon: Drawable
)