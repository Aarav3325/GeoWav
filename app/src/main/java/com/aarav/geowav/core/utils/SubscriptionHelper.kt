package com.aarav.geowav.core.utils

import com.aarav.geowav.R
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.data.model.UserSubscription

data class SubscriptionStatus(
    val active: Boolean,
    val isCancelled: Boolean,
    val isExpired: Boolean,
    val daysRemaining: Int
)

object SubscriptionHelper {
    fun getSubscriptionStatus(data: UserSubscription): SubscriptionStatus {
        val now = System.currentTimeMillis()

        val isExpired = if (data.overrideEnabled) false else now > data.expiryTime
        val isCancelled = !data.autoRenewing
        val active = if (data.overrideEnabled) true else !isExpired

        val daysRemaining = if (data.overrideEnabled) 365 else ((data.expiryTime - now) / (1000 * 60 * 60 * 24))
            .toInt()
            .coerceAtLeast(0)

        return SubscriptionStatus(
            active = active,
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

    fun getPlanBadge(plan: UserPlan): Int? {
        return when (plan) {
            UserPlan.FREE -> null
            UserPlan.PREMIUM -> R.drawable.geowav_premium_badge
            UserPlan.PRO -> R.drawable.geowav_pro_badge
        }
    }
}