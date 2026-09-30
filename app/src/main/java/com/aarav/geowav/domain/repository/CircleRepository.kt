package com.aarav.geowav.domain.repository

import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.PendingInvite
import kotlinx.coroutines.flow.Flow

interface CircleRepository {

    suspend fun findUserByEmail(email: String): String?

    suspend fun sendCircleInvite(
        senderUid: String,
        senderEmail: String,
        receiverEmail: String,
        senderProfileName: String,
        receiverUid: String,
        alias: String
    ): Resource<Unit>

    suspend fun acceptInvite(
        receiverUid: String,
        senderUid: String,
        senderEmail: String,
        senderProfileName: String,
        receiverProfileName: String
    ): Resource<Unit>

    suspend fun rejectInvite(
        receiverUid: String,
        senderUid: String
    ): Resource<Unit>

    suspend fun getAcceptedLovedOnes(
        userId: String
    ): Resource<List<CircleMember>>

    fun getPendingInvites(
        userId: String
    ): Flow<List<PendingInvite>>

    suspend fun deleteCircleMember(
        userId: String,
        circleMemberId: String
    ): Resource<Unit>

    suspend fun sendLocationRequest(
        requesterUid: String,
        requesterName: String,
        recipientUid: String
    ): Resource<String>

    fun observeIncomingLocationRequests(
        userId: String
    ): Flow<List<com.aarav.geowav.data.model.LocationRequest>>

    suspend fun respondToLocationRequest(
        recipientUid: String,
        requestId: String,
        accept: Boolean,
        durationMinutes: Int?
    ): Resource<Unit>
}
