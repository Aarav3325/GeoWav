package com.aarav.geowav.data.repository

import com.aarav.geowav.data.model.LocationMeta
import com.aarav.geowav.data.model.LocationUpdates
import com.aarav.geowav.data.model.toMap
import com.aarav.geowav.domain.repository.LiveLocationSharingRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LiveLocationSharingRepositoryImpl
@Inject constructor(
    val firebaseDatabase: FirebaseDatabase
) : LiveLocationSharingRepository {

    val rootRef = firebaseDatabase.reference

    override fun observeUserLiveLocation(userId: String): Flow<LocationUpdates> = callbackFlow {
        val ref = rootRef.child("live_location")
            .child(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val location = snapshot.getValue(LocationUpdates::class.java)
                location?.let { trySend(it) }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }

        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }


    override suspend fun startSharing(
        userId: String,
        lat: Double,
        long: Double
    ) {

        val data = LocationMeta(
            lat = lat,
            lng = long,
            active = true,
            timestamp = System.currentTimeMillis(),
            startedAt = System.currentTimeMillis()
        )

//        val data = mapOf(
//            "lat" to lat,
//            "long" to long,
//            "timestamp" to System.currentTimeMillis(),
//            "active" to true,
//            "startedAt" to System.currentTimeMillis()
//        )

        val ref = rootRef.child("live_location")
            .child(userId)

        ref.setValue(data).await()
    }

    override suspend fun updateLocation(userId: String, lat: Double, long: Double) {
//        val updates = mapOf(
//            "lat" to lat,
//            "long" to long,
//            "timestamp" to System.currentTimeMillis()
//        )



        val updates = LocationUpdates(
            lat = lat,
            lng = long,
            timestamp = System.currentTimeMillis()
        )

        rootRef.child("live_location")
            .child(userId)
            .updateChildren(updates.toMap())
            .await()
    }

    override suspend fun stopSharingLiveLocation(userId: String) {
        val ref = rootRef.child("live_location")
            .child(userId)

        ref.removeValue().await()
    }

    override suspend fun isLiveLocationActive(userId: String): Boolean {
        val snapshot = rootRef
            .child("live_location")
            .child(userId)
            .get()
            .await()

        return snapshot.exists()
    }

}