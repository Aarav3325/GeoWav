package com.aarav.geowav.data.model

enum class UserPlan {
    FREE,
    PREMIUM,
    PRO
}

data class UserSubscription(
    val plan: UserPlan = UserPlan.FREE,
    val isActive: Boolean = false,
    val purchaseToken: String = "",
    val updatedAt: Long = 0L
)