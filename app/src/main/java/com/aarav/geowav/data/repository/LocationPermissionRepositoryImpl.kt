package com.aarav.geowav.data.repository

import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.domain.repository.LocationPermissionRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import okio.IOException
import javax.inject.Inject

class LocationPermissionRepositoryImpl
@Inject constructor(
    val firebaseDatabase: FirebaseDatabase
) : LocationPermissionRepository {

    val rootRef = firebaseDatabase.reference


    override suspend fun allowViewer(currentUserId: String, viewerId: String) {
        rootRef
            .child("location_sharing")
            .child(currentUserId)
            .child(viewerId)
            .setValue(true)
            .await()
    }

    override suspend fun revokeViewer(currentUserId: String, viewerId: String) {
        rootRef
            .child("location_sharing")
            .child(currentUserId)
            .child(viewerId)
            .removeValue()
            .await()
    }

    override fun getAllowedViewers(currentUserId: String): Flow<Set<String>> = callbackFlow {
        val ref = rootRef
            .child("location_sharing")
            .child(currentUserId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val viewers = snapshot.children.mapNotNull { it.key }
                trySend(viewers.toSet())
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

}