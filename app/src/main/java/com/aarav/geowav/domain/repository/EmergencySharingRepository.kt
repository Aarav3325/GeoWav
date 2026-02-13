package com.aarav.geowav.domain.repository

import com.aarav.geowav.data.model.EmergencyInfo
import kotlinx.coroutines.flow.Flow

interface EmergencySharingRepository {
    suspend fun startEmergency(
        currentUserId: String, duration: Long,
        viewers: List<String>
    )

    suspend fun stopEmergency(currentUserId: String)
    suspend fun isEmergencyActive(currentUserId: String): Boolean

    fun observeEmergency(currentUserId: String): Flow<EmergencyInfo?>
}