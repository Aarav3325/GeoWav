package com.aarav.geowav.data.model

import com.aarav.geowav.R

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

sealed class UpgradeEvents {
    data class ShowUpgrade(val upgradeContext: UpgradeContext) : UpgradeEvents()
}

sealed class UpgradeReason {
    object PlaybackLocked : UpgradeReason()
    object HistoryLimit : UpgradeReason()
    object SpeedControl : UpgradeReason()
}

data class UpgradeContext(
    val upgradeTo: UserPlan,
    val reason: UpgradeReason
)

data class PlanContent(
    val title: String,
    val description: String,
    val icon: Int,
    val features: List<String>,
    val ctaText: String
)

data class ReasonContent(
    val title: String,
    val description: String,
    val icon: Int
)

fun getReasonContent(reason: UpgradeReason): ReasonContent {
    return when (reason) {
        UpgradeReason.PlaybackLocked -> ReasonContent(
            "Playback is Premium",
            "Replay your trips with smooth animations and insights.",
            R.drawable.play
        )

        UpgradeReason.HistoryLimit -> ReasonContent(
            "Unlock Full History",
            "Access your complete travel timeline anytime.",
            R.drawable.timeline
        )

        UpgradeReason.SpeedControl -> ReasonContent(
            "Control Playback Speed",
            "Adjust speed for better route analysis.",
            R.drawable.playback_speed
        )
    }
}

fun getPlanContent(plan: UserPlan): PlanContent {
    return when (plan) {

        UserPlan.PREMIUM -> PlanContent(
            title = "Go Premium",
            description = "Unlock powerful playback and full history access.",
            icon = R.drawable.play,
            features = listOf(
                "Playback & route animation",
                "Unlimited history access",
                "Smooth trip insights"
            ),
            ctaText = "Upgrade to Premium"
        )

        UserPlan.PRO -> PlanContent(
            title = "Go Pro",
            description = "Get advanced controls and deeper insights.",
            icon = R.drawable.playback_speed,
            features = listOf(
                "Everything in Premium",
                "Playback speed control",
                "Advanced analytics"
            ),
            ctaText = "Upgrade to Pro"
        )

        else -> error("No upgrade config for FREE")
    }
}