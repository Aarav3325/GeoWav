package com.aarav.geowav.domain.repository

import com.aarav.geowav.data.model.UserPlan
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun observeUserPlan(): Flow<UserPlan>
}