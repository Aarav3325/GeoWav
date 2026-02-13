package com.aarav.geowav.data.repository

import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.core.utils.encodeEmail
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.PendingInvite
import com.aarav.geowav.domain.repository.CircleRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CircleRepositoryImpl
@Inject constructor(
    val firebaseDatabase: FirebaseDatabase
) : CircleRepository {

    private val rootRef = firebaseDatabase.reference

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

            val updates = mapOf(
                "circle_requests/$receiverUid/$senderUid" to mapOf(
                    "status" to "pending",
                    "senderEmail" to senderEmail,
                    "senderProfileName" to senderProfileName,
                    "sentAt" to timestamp
                ),
                "circle/$senderUid/$receiverUid" to mapOf(
                    "receiverEmail" to receiverEmail,
                    "status" to "pending",
                    "alias" to alias,
                    "addedAt" to timestamp
                )
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
        senderProfileName: String,
        receiverProfileName: String
    ): Resource<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()

            val updates = mapOf(
                // Sender’s view of receiver
                "circle/$senderUid/$receiverUid/status" to "accepted",
                "circle/$senderUid/$receiverUid/profileName" to receiverProfileName,
                "circle/$senderUid/$receiverUid/addedAt" to timestamp,

                // Receiver’s view of sender
                "circle/$receiverUid/$senderUid/status" to "accepted",
                "circle/$receiverUid/$senderUid/profileName" to senderProfileName,
                "circle/$receiverUid/$senderUid/addedAt" to timestamp,

                // Remove pending request
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
                    CircleMember(
                        id = child.key!!,
                        profileName = child.child("profileName")
                            .getValue(String::class.java) ?: "Unknown",
                        alias = child.child("alias").getValue(String::class.java),
                        selected = false,
                        receiverEmail = child.child("receiverEmail").getValue(String::class.java) ?: "",
                        )
                } else null
            }

            Resource.Success(lovedOnes)

        } catch (e: Exception) {
            Resource.Error("Failed to load loved ones")
        }
    }

    override suspend fun getPendingInvites(userId: String): Resource<List<PendingInvite>> {
        return try {
            val snapshot = rootRef
                .child("circle_requests")
                .child(userId)
                .get()
                .await()

            val pendingInvites = snapshot.children.mapNotNull {
                val status = it.child("status").getValue(String::class.java)
                if (status == "pending") {
                    PendingInvite(
                        senderEmail = it.child("senderEmail").getValue(String::class.java) ?: "",
                        senderProfileName = it.child("senderProfileName")
                            .getValue(String::class.java),
                        sentAt = it.child("sentAt").getValue(Long::class.java),
                        status = "pending",
                        senderId = it.key!!
                    )
                } else null
            }

            Resource.Success(pendingInvites)
        } catch (e: Exception) {
            Resource.Error("Failed to pending invites")
        }
    }
}