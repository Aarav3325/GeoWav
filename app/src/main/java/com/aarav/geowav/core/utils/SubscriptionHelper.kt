package com.aarav.geowav.core.utils

import com.aarav.geowav.data.model.UserSubscription

data class SubscriptionStatus(
    val isActive: Boolean,
    val isCancelled: Boolean,
    val isExpired: Boolean,
    val daysRemaining: Int
)

object SubscriptionHelper {
    fun getSubscriptionStatus(data: UserSubscription): SubscriptionStatus {
        val now = System.currentTimeMillis()

        val isExpired = now > data.expiryTime
        val isCancelled = !data.autoRenewing
        val isActive = !isExpired

        val daysRemaining = ((data.expiryTime - now) / (1000 * 60 * 60 * 24))
            .toInt()
            .coerceAtLeast(0)

        return SubscriptionStatus(
            isActive = isActive,
            isCancelled = isCancelled,
            isExpired = isExpired,
            daysRemaining = daysRemaining
        )
    }
}