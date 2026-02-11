package com.aarav.geowav.domain.repository

import com.aarav.geowav.data.model.LocationUpdates
import kotlinx.coroutines.flow.Flow

interface LiveLocationSharingRepository {

    fun observeUserLiveLocation(userId: String): Flow<LocationUpdates>
    suspend fun startSharing(
        userId: String,
        lat: Double,
        long: Double
    )

    suspend fun updateLocation(
        userId: String,
        lat: Double,
        long: Double
    )

    suspend fun stopSharingLiveLocation(userId: String)

    suspend fun isLiveLocationActive(userId: String): Boolean

}