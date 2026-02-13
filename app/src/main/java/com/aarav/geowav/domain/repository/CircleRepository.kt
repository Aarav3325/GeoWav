package com.aarav.geowav.domain.repository

import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.PendingInvite

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

    suspend fun getPendingInvites(
        userId: String
    ): Resource<List<PendingInvite>>
}
