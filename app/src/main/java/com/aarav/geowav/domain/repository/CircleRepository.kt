package com.aarav.geowav.domain.repository

import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.presentation.locationsharing.LovedOneUi

interface CircleRepository {

    suspend fun findUserByEmail(email: String): String?

    suspend fun sendCircleInvite(
        senderUid: String,
        senderEmail: String,
        receiverUid: String
    ): Resource<Unit>

    suspend fun acceptInvite(
        receiverUid: String, senderUid: String, senderName: String, receiverName: String
    ): Resource<Unit>

    suspend fun rejectInvite(
        receiverUid: String,
        senderUid: String
    )

    suspend fun getAcceptedLovedOnes(
        userId: String
    ): Resource<List<LovedOneUi>>
}
