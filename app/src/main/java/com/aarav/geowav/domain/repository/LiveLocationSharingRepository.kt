package com.aarav.geowav.domain.repository

import com.aarav.geowav.data.model.DestinationLocation
import com.aarav.geowav.data.model.LocationUpdates
import com.aarav.geowav.data.model.SessionMode
import com.aarav.geowav.data.model.SessionStatus
import com.aarav.geowav.data.model.SharingSession
import com.aarav.geowav.data.model.StayPoint
import kotlinx.coroutines.flow.Flow

interface LiveLocationSharingRepository {

    fun observeUserLiveLocation(userId: String): Flow<LocationUpdates>

    suspend fun startSharing(
        userName: String,
        userId: String,
        lat: Double,
        long: Double,
        mode: SessionMode = SessionMode.NORMAL,
        expiresAt: Long? = null,
        destinationPlaceId: String? = null,
        destinationLocation: DestinationLocation? = null,
        createdFrom: String? = null
    )

    suspend fun updateLocation(
        userId: String,
        lat: Double,
        long: Double
    )

    suspend fun stopSharingLiveLocation(
        userId: String,
        finalStatus: SessionStatus = SessionStatus.COMPLETED
    )

    suspend fun updateSessionStatus(
        userId: String,
        newStatus: SessionStatus
    ): Boolean

    fun observeActiveSession(userId: String): Flow<SharingSession?>

    suspend fun saveStayPoint(
        userId: String,
        stayPoint: StayPoint
    )

    suspend fun isLiveLocationActive(userId: String): Boolean

    fun getUpdatedTimestamp(userId: String): Flow<Long>

    fun observeSharingActive(userId: String): Flow<Boolean>
}