package com.aarav.geowav.data.model

data class
CircleMember(
    val id: String,
    val profileName: String,
    val alias: String?,
    val selected: Boolean,
    val receiverEmail: String? = "",
    val addedAt: Long = 0L,
    val avatarUrl: String? = null
)

data class CircleMemberDTO(
    val id: String = "",
    val profileName: String = "",
    val alias: String? = null,
    val selected: Boolean = false,
    val receiverEmail: String? = "",
    val addedAt: Long = 0L,
    val avatarUrl: String? = null
)

data class
PendingInvite(
    val senderEmail: String? = "",
    val senderProfileName: String? =   "",
    val sentAt: Long? = 0L,
    val status: String? = "",
    val senderId: String
)
