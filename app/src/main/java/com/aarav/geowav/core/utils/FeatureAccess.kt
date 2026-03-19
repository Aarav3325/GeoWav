package com.aarav.geowav.core.utils

import com.aarav.geowav.data.model.UpgradeReason
import com.aarav.geowav.data.model.UserPlan
import java.time.LocalDate
import java.time.ZoneId

object FeatureAccess {

    private val indiaZone: ZoneId = ZoneId.of("Asia/Kolkata")

    fun nextPlan(plan: UserPlan): UserPlan? {
        return when (plan) {
            UserPlan.FREE -> UserPlan.PREMIUM
            UserPlan.PREMIUM -> UserPlan.PRO
            UserPlan.PRO -> null
        }
    }

    fun canUsePlayback(userPlan: UserPlan): Boolean {
        return userPlan != UserPlan.FREE
    }

    fun canControlSpeed(userPlan: UserPlan): Boolean {
        return userPlan != UserPlan.FREE
    }


    fun locationSharingLimit(userPlan: UserPlan): Long? {
        return when (userPlan) {
            UserPlan.FREE -> 30 * 60 * 1000L
            UserPlan.PREMIUM -> null
            UserPlan.PRO -> null
        }
    }

    fun getUpgradePlan(reason: UpgradeReason): UserPlan? {
        return when (reason) {
            UpgradeReason.ActivityYesterday -> UserPlan.PREMIUM
            UpgradeReason.FullActivityHistoryAccess -> UserPlan.PRO
            UpgradeReason.FullTimelineAccess -> UserPlan.PRO
            UpgradeReason.TimelineYesterday -> UserPlan.PREMIUM
            else -> null
        }
    }

    fun maxSavedPlaces(userPlan: UserPlan): Int {
        return when (userPlan) {
            UserPlan.FREE -> 2
            UserPlan.PREMIUM -> 5
            UserPlan.PRO -> Int.MAX_VALUE
        }
    }

    fun canUseStayPoints(userPlan: UserPlan): Boolean {
        return userPlan != UserPlan.FREE
    }

    fun maxConnections(userPlan: UserPlan): Int {
        return when (userPlan) {
            UserPlan.FREE -> 1
            UserPlan.PREMIUM -> 2
            UserPlan.PRO -> 10
        }
    }

}