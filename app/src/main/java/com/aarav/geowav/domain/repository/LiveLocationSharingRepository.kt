package com.aarav.geowav.domain.repository

import com.aarav.geowav.data.model.LocationUpdates
import com.aarav.geowav.data.model.StayPoint
import kotlinx.coroutines.flow.Flow

interface LiveLocationSharingRepository {

    fun observeUserLiveLocation(userId: String): Flow<LocationUpdates>
    suspend fun startSharing(
        userName: String,
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

    suspend fun saveStayPoint(
        userId: String,
        stayPoint: StayPoint
    )

    suspend fun isLiveLocationActive(userId: String): Boolean

    fun getUpdatedTimestamp(userId: String): Flow<Long>

}