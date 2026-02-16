package com.aarav.geowav.domain.repository

import com.aarav.geowav.core.utils.ViewerLocationState
import kotlinx.coroutines.flow.Flow

interface ViewerLocationRepository {
    fun observeUserLocation(userId: String, viewerId: String): Flow<ViewerLocationState>
}