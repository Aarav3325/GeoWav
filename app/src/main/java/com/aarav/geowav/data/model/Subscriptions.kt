package com.aarav.geowav.data.model

import com.aarav.geowav.R
import com.aarav.geowav.presentation.paywall.premiumPlanColors
import com.aarav.geowav.presentation.paywall.proPlanColors

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
    object MaxPlaces : UpgradeReason()
    object MaxConnections : UpgradeReason()
//    object StayPoints : UpgradeReason()

    object ActivityYesterday : UpgradeReason()
    object FullActivityHistoryAccess : UpgradeReason()
    object TimelineYesterday : UpgradeReason()
    object FullTimelineAccess : UpgradeReason()
    object SessionLimitReached : UpgradeReason()
}

data class UpgradeContext(
    val upgradeTo: UserPlan,
    val reason: UpgradeReason
)

data class PlanContent(
    val title: String,
    val description: String,
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

        UpgradeReason.MaxPlaces -> ReasonContent(
            "Add More Places",
            "Upgrade to save more locations and get smart alerts for all your important places.",
            R.drawable.map_pin_area
        )

        UpgradeReason.MaxConnections -> ReasonContent(
            "Connect with More People",
            "Upgrade to add more connections and stay connected with everyone who matters.",
            R.drawable.heart_fill
        )

        UpgradeReason.ActivityYesterday -> ReasonContent(
            "Unlock Yesterday",
            "Upgrade to Premium to view your activity from yesterday.",
            R.drawable.activity
        )

        UpgradeReason.FullActivityHistoryAccess -> ReasonContent(
            "Unlock Full Activity History",
            "Upgrade to Pro to access 7 days and custom date ranges.",
            R.drawable.activity
        )

        UpgradeReason.TimelineYesterday -> ReasonContent(
            "Revisit Yesterday's Timeline",
            "Upgrade to Premium to explore routes, locations and movement of you and your connections from yesterday.",
            R.drawable.timeline
        )

        UpgradeReason.FullTimelineAccess -> ReasonContent(
            "Unlock Complete Timeline",
            "Upgrade to Pro to explore full movement history for you and your connections with advanced filters and ranges.",
            R.drawable.timeline
        )

        UpgradeReason.SessionLimitReached -> ReasonContent(
            "Session Limit Reached",
            "Upgrade to continue sharing your live location without time limits.",
            R.drawable.new_logo
        )
    }
}

fun getPlanContent(plan: UserPlan): PlanContent {
    return when (plan) {

        UserPlan.PREMIUM -> PlanContent(
            title = "Go Premium",
            description = "Unlock powerful playback and full history access.",
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