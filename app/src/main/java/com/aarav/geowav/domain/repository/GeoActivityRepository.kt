package com.aarav.geowav.domain.repository

import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.data.model.GeoAlert
import kotlinx.coroutines.flow.Flow

interface GeoActivityRepository {
    fun observeAlerts(filter: ActivityFilter): Flow<List<GeoAlert>>
}