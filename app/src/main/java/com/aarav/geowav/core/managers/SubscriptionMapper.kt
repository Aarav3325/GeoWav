package com.aarav.geowav.core.managers

import com.aarav.geowav.data.model.UserPlan

object SubscriptionMapper {
    const val PREMIUM_ID = "geowav_premium"
    const val PRO_ID = "geowav_pro"

    const val PREMIUM_PACKAGE_ID = "premium_monthly"
    const val PRO_PACKAGE_ID = "pro_monthly"

    const val ENTITLEMENT_PREMIUM = "premium"
    const val ENTITLEMENT_PRO = "pro"

    fun fromProductId(productId: String): UserPlan {
        when (productId) {
            PREMIUM_ID, PREMIUM_PACKAGE_ID  -> return UserPlan.PREMIUM
            PRO_ID, PRO_PACKAGE_ID -> return UserPlan.PRO
            else -> return UserPlan.FREE
        }
    }

    fun fromEntitlementId(entitlementId: String): UserPlan {
        return when (entitlementId) {
            ENTITLEMENT_PRO -> UserPlan.PRO
            ENTITLEMENT_PREMIUM -> UserPlan.PREMIUM
            else -> UserPlan.FREE
        }
    }
}