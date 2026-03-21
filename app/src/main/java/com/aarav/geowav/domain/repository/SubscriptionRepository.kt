package com.aarav.geowav.domain.repository

import com.aarav.geowav.core.utils.SubscriptionStatus
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.data.model.UserSubscription
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun observeUserPlan(): Flow<UserPlan>

    fun fetchSubscriptionStatus(): Flow<UserSubscription>
}