package com.aarav.geowav.data.repository

import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.domain.repository.CircleRepository
import com.aarav.geowav.presentation.locationsharing.LovedOneUi
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CircleRepositoryImpl
@Inject constructor(
    val firebaseDatabase: FirebaseDatabase
) : CircleRepository {

    private val rootRef = firebaseDatabase.reference

    override suspend fun findUserByEmail(email: String): String? {
        val emailKey = email.trim().lowercase()

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
        receiverUid: String
    ): Resource<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()

            val updates = mapOf(
                "circle_requests/$receiverUid/$senderUid" to mapOf(
                    "status" to "pending",
                    "senderEmail" to senderEmail,
                    "sentAt" to timestamp
                ),
                "circle/$senderUid/$receiverUid" to mapOf(
                    "status" to "pending",
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
        senderName: String,
        receiverName: String
    ) {
        val timestamp = System.currentTimeMillis()

        val updates = mapOf(
            "circle/$senderUid/$receiverUid" to mapOf(
                "status" to "accepted",
                "name" to senderName,
                "addedAt" to timestamp
            ),
            "circle/$receiverUid/$senderUid" to mapOf(
                "status" to "accepted",
                "name" to receiverName,
                "addedAt" to timestamp
            ),
            "circle_requests/$receiverUid/$senderUid" to null
        )

        rootRef.updateChildren(updates).await()
    }

    override suspend fun rejectInvite(receiverUid: String, senderUid: String) {
        val updates = mapOf(
            "circle_requests/$receiverUid/$senderUid" to null,
            "circle/$senderUid/$receiverUid" to null
        )

        rootRef.updateChildren(updates).await()
    }

    override suspend fun getAcceptedLovedOnes(
        userId: String
    ): Resource<List<LovedOneUi>> {
        return try {
            val snapshot = rootRef
                .child("circle")
                .child(userId)
                .get()
                .await()

            val lovedOnes = snapshot.children.mapNotNull { child ->
                val status = child.child("status").getValue(String::class.java)
                if (status == "accepted") {
                    LovedOneUi(
                        id = child.key!!,
                        name = child.child("name").getValue(String::class.java) ?: "Unknown",
                        selected = false
                    )
                } else null
            }

            Resource.Success(lovedOnes)

        } catch (e: Exception) {
            Resource.Error("Failed to load loved ones")
        }
    }


}