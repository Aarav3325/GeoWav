package com.aarav.geowav.data.repository

import com.aarav.geowav.data.model.EmergencyInfo
import com.aarav.geowav.domain.repository.EmergencySharingRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EmergencySharingRepositoryImpl
@Inject constructor(
    val firebaseDatabase: FirebaseDatabase
) : EmergencySharingRepository {

    val rootRef = firebaseDatabase.reference

    override suspend fun startEmergency(
        currentUserId: String,
        duration: Long,
        viewers: List<String>
    ) {

        val viewerMap = viewers.associateWith { true }

        val now = System.currentTimeMillis()

        val data = mapOf(
            "active" to true,
            "startedAt" to now,
            "endsAt" to now + duration,
            "viewers" to viewerMap
        )

        rootRef
            .child("emergency_sharing")
            .child(currentUserId)
            .setValue(data)
            .await()
    }

    override suspend fun stopEmergency(currentUserId: String) {
        rootRef
            .child("emergency_sharing")
            .child(currentUserId)
            .removeValue()
            .await()
    }

    override suspend fun isEmergencyActive(currentUserId: String): Boolean {
        val snapshot = rootRef
            .child("emergency_sharing")
            .child(currentUserId)
            .get()
            .await()

        if (!snapshot.exists()) return false

        val endsAt = snapshot.child("endsAt").value as? Long ?: return false

        return System.currentTimeMillis() < endsAt
    }

    override fun observeEmergency(currentUserId: String): Flow<EmergencyInfo?> = callbackFlow {
        val ref = rootRef
            .child("emergency_sharing")
            .child(currentUserId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(null)
                    return
                }

                val active = snapshot.child("active").getValue(Boolean::class.java) ?: false
                if (!active) {
                    trySend(null)
                    return
                }

                val startedAt = snapshot.child("startedAt").getValue(Long::class.java) ?: 0L
                val endsAt = snapshot.child("endsAt").getValue(Long::class.java) ?: 0L
                val duration = snapshot.child("duration").getValue(Long::class.java) ?: 0L

                val viewers = snapshot.child("viewers")
                    .children
                    .mapNotNull { it.key }
                    .toSet()

                val info = EmergencyInfo(
                    startedAt = startedAt,
                    endsAt = endsAt,
                    duration = duration,
                    viewers = viewers
                )

                trySend(info)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }


}