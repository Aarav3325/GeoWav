package com.aarav.geowav.core.utils

import com.aarav.geowav.data.model.UserPlan
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

    fun getAvailablePlans(plan: UserPlan): List<UserPlan> {
        return when (plan) {
            UserPlan.FREE -> listOf(UserPlan.PREMIUM, UserPlan.PRO)
            UserPlan.PREMIUM -> listOf(UserPlan.PRO)
            UserPlan.PRO -> emptyList()
        }
    }

    fun getPlanName(plan: UserPlan): String {
        return when (plan) {
            UserPlan.FREE -> "Free"
            UserPlan.PREMIUM -> "Premium"
            UserPlan.PRO -> "Pro"
        }
    }
}