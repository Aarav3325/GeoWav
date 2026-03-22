package com.aarav.geowav.core.managers

import com.aarav.geowav.data.model.UserPlan

object SubscriptionMapper {
    const val PREMIUM_ID = "geowav_premium"
    const val PRO_ID = "geowav_pro"

    fun fromProductId(productId: String): UserPlan {
        when (productId) {
            PREMIUM_ID -> return UserPlan.PREMIUM
            PRO_ID -> return UserPlan.PRO
            else -> return UserPlan.FREE
        }
    }
}