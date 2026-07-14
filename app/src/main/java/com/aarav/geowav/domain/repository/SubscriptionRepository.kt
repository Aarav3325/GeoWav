package com.aarav.geowav.domain.repository

import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.data.model.UserSubscription
import com.revenuecat.purchases.Package
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun observeUserPlan(): Flow<UserPlan>

    fun fetchSubscriptionStatus(): Flow<UserSubscription>

    suspend fun fetchAllPackages(offeringId: String? = null): Resource<List<Package>>
}
