package com.aarav.geowav.domain.repository

import com.aarav.geowav.core.utils.Resource
import kotlinx.coroutines.flow.Flow

interface LocationPermissionRepository {

    suspend fun allowViewer(currentUserId: String, viewerId: String)

    suspend fun revokeViewer(currentUserId: String, viewerId: String)

    fun getAllowedViewers(currentUserId: String): Flow<Set<String>>

    suspend fun allowAllLovedOnes(currentUserId: String, viewerIds: List<String>)
}