package com.aarav.geowav.data.repository

import android.util.Log
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.core.utils.encodeEmail
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.PendingInvite
import com.aarav.geowav.domain.repository.CircleRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CircleRepositoryImpl
@Inject constructor(
    val firebaseDatabase: FirebaseDatabase
) : CircleRepository {

    private val rootRef = firebaseDatabase.reference
    private val usersRef = firebaseDatabase.getReference("users")

    override suspend fun findUserByEmail(email: String): String? {
        val emailKey = encodeEmail(email)

        val snapshot = rootRef
            .child("user_lookup")
            .child(emailKey)
            .get()
            .await()

        return snapshot.getValue(String::class.java)
    }

    override suspend fun sendCircleInvite(
        senderUid: String,
        senderEmail: String,
        receiverEmail: String,
        senderProfileName: String,
        receiverUid: String,
        alias: String
    ): Resource<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            val senderAvatarUrl = getUserAvatarUrl(senderUid)
            val receiverAvatarUrl = getUserAvatarUrl(receiverUid)

            val invitePayload = mutableMapOf<String, Any>(
                "status" to "pending",
                "email" to senderEmail,
                "senderProfileName" to senderProfileName,
                "sentAt" to timestamp
            ).apply {
                senderAvatarUrl?.let { put("avatarUrl", it) }
            }

            val circlePayload = mutableMapOf<String, Any>(
                "email" to receiverEmail,
                "status" to "pending",
                "alias" to alias,
                "addedAt" to timestamp
            ).apply {
                receiverAvatarUrl?.let { put("avatarUrl", it) }
            }

            val updates = mapOf(
                "circle_requests/$receiverUid/$senderUid" to invitePayload,
                "circle/$senderUid/$receiverUid" to circlePayload
            )

            rootRef.updateChildren(updates).await()

            Resource.Success(Unit)

        } catch (e: Exception) {
            Resource.Error(
                message = "Failed to send invite"
            )
        }
    }

    override suspend fun acceptInvite(
        receiverUid: String,
        senderUid: String,
        senderEmail: String,
        senderProfileName: String,
        receiverProfileName: String
    ): Resource<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            val senderAvatarUrl = getUserAvatarUrl(senderUid)
            val receiverAvatarUrl = getUserAvatarUrl(receiverUid)

            val updates = mapOf(
                "circle/$senderUid/$receiverUid/status" to "accepted",
                "circle/$senderUid/$receiverUid/profileName" to receiverProfileName,
                "circle/$senderUid/$receiverUid/addedAt" to timestamp,
                "circle/$senderUid/$receiverUid/avatarUrl" to receiverAvatarUrl,

                "circle/$receiverUid/$senderUid/status" to "accepted",
                "circle/$receiverUid/$senderUid/email" to senderEmail,
                "circle/$receiverUid/$senderUid/profileName" to senderProfileName,
                "circle/$receiverUid/$senderUid/addedAt" to timestamp,
                "circle/$receiverUid/$senderUid/avatarUrl" to senderAvatarUrl,

                "circle_requests/$receiverUid/$senderUid" to null
            )

            rootRef.updateChildren(updates).await()
            Resource.Success(Unit)

        } catch (e: Exception) {
            Resource.Error("Failed to accept invite")
        }
    }

    override suspend fun rejectInvite(
        receiverUid: String,
        senderUid: String
    ): Resource<Unit> {
        return try {

            val updates = mapOf(
                "circle_requests/$receiverUid/$senderUid" to null,
                "circle/$senderUid/$receiverUid" to null
            )

            rootRef.updateChildren(updates).await()

            Resource.Success(Unit)

        } catch (e: Exception) {
            Resource.Error("Failed to reject invite")
        }
    }

    override suspend fun getAcceptedLovedOnes(
        userId: String
    ): Resource<List<CircleMember>> {
        return try {

            val snapshot = rootRef
                .child("circle")
                .child(userId)
                .get()
                .await()


            val lovedOnes = snapshot.children.mapNotNull { child ->
                val status = child.child("status").getValue(String::class.java)
                if (status == "accepted") {
                    val memberId = child.key ?: return@mapNotNull null
                    val avatarUrl = child.child("avatarUrl").getValue(String::class.java)
                        ?: child.child("avatar").getValue(String::class.java)
                        ?: getUserAvatarUrl(memberId)

                    CircleMember(
                        id = memberId,
                        profileName = child.child("profileName")
                            .getValue(String::class.java) ?: "Unknown",
                        alias = child.child("alias").getValue(String::class.java),
                        selected = false,
                        receiverEmail = child.child("email").getValue(String::class.java)
                            ?: "",
                        addedAt = child.child("addedAt").getValue(Long::class.java) ?: 0L,
                        avatarUrl = avatarUrl
                    )
                } else null
            }

            Resource.Success(lovedOnes)

        } catch (e: Exception) {
            Resource.Error("Failed to load loved ones")
        }
    }

    override fun getPendingInvites(userId: String): Flow<List<PendingInvite>> = callbackFlow {

        val ref = rootRef.child("circle_requests").child(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pendingInvites = snapshot.children.mapNotNull {
                    val status = it.child("status").getValue(String::class.java)
                    if (status == "pending") {
                        PendingInvite(
                            senderEmail = it.child("email").getValue(String::class.java)
                                ?: "",
                            senderProfileName = it.child("senderProfileName")
                                .getValue(String::class.java),
                            sentAt = it.child("sentAt").getValue(Long::class.java),
                            status = "pending",
                            senderId = it.key ?: return@mapNotNull null
                        )
                    } else null
                }
                trySend(pendingInvites)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    override suspend fun deleteCircleMember(
        userId: String,
        circleMemberId: String
    ): Resource<Unit> {
        return try {
            val ref = rootRef.child("circle")

            val updates = mapOf(
                "$userId/$circleMemberId" to null,
                "$circleMemberId/$userId" to null
            )


            ref.updateChildren(updates).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to delete circle member")
        }
    }

    private suspend fun getUserAvatarUrl(userId: String): String? {
        return try {
            usersRef.child(userId)
                .child("avatar")
                .get()
                .await()
                .getValue(String::class.java)
                ?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Log.w("CircleRepository", "Failed to load avatar for $userId", e)
            null
        }
    }


}
