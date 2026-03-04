package com.aarav.geowav.data.repository

import android.location.Geocoder
import com.aarav.geowav.data.model.LocationMeta
import com.aarav.geowav.data.model.LocationUpdates
import com.aarav.geowav.data.model.SessionHistory
import com.aarav.geowav.data.model.StayPoint
import com.aarav.geowav.data.model.UserPath
import com.aarav.geowav.data.model.UserPathLatLng
import com.aarav.geowav.domain.repository.LiveLocationSharingRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class LiveLocationSharingRepositoryImpl
@Inject constructor(
    val firebaseDatabase: FirebaseDatabase,
    @ApplicationContext private val context: Context
) : LiveLocationSharingRepository {

    val rootRef = firebaseDatabase.reference

    // Not using
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

    // Start sharing live location
    override suspend fun startSharing(
        userName: String,
        userId: String,
        lat: Double,
        long: Double
    ) {

        val update = hashMapOf<String, Any>()

        val pathRef = rootRef.child("live_location/$userId").child("path").push()
        val now = System.currentTimeMillis()

        update["live_location/$userId/lat"] = lat
        update["live_location/$userId/lng"] = long
        update["live_location/$userId/timestamp"] = now
        update["live_location/$userId/active"] = true
        update["live_location/$userId/startedAt"] = now
        update["live_location/$userId/userName"] = userName

        update["live_location/$userId/path/${pathRef.key}"] = mapOf(
            "lat" to lat,
            "lng" to long,
            "timestamp" to now
        )

        rootRef.updateChildren(update).await()

        rootRef.child("live_location/$userId").child("active")
            .onDisconnect()
            .setValue(false)
    }

    // Update location
    override suspend fun updateLocation(userId: String, lat: Double, long: Double) {

        val pathRef = rootRef.child("live_location/$userId").child("path").push()


        val now = System.currentTimeMillis()
        val updates = hashMapOf<String, Any>()

        // Keep existing fields for observers
        updates["live_location/$userId/lat"] = lat
        updates["live_location/$userId/lng"] = long
        updates["live_location/$userId/timestamp"] = now

        // Persist path
        updates["live_location/$userId/path/${pathRef.key}"] = mapOf(
            "lat" to lat,
            "lng" to long,
            "timestamp" to now
        )


        rootRef.updateChildren(updates).await()
    }

    // Stop sharing live location
    override suspend fun stopSharingLiveLocation(userId: String) {

        val liveRef = rootRef.child("live_location").child(userId)
        val snapshot = liveRef.get().await()

        if (!snapshot.exists()) return

        // Only finalize if active was true or path exists
        if (snapshot.child("path").childrenCount < 2) {
            liveRef.removeValue().await()
            return
        }

        val startedAt = snapshot.child("startedAt")
            .getValue(Long::class.java) ?: return

        val userName = snapshot.child("userName")
            .getValue(String::class.java) ?: ""

        val sharedWith = snapshot.child("sharedWith")
            .children.mapNotNull { it.getValue(String::class.java) }

        val pathPoints = snapshot.child("path").children.mapNotNull {
            val lat = it.child("lat").getValue(Double::class.java)
            val lng = it.child("lng").getValue(Double::class.java)
            val timestamp = it.child("timestamp").getValue(Long::class.java)


            if (lat != null && lng != null && timestamp != null)
                UserPathLatLng(latitude = lat, longitude = lng, timestamp = timestamp)
            else null
        }
            .sortedBy { it.timestamp }

        if (pathPoints.size < 2) {
            liveRef.removeValue().await()
            return
        }

        val start = pathPoints.first()
        val end = pathPoints.last()

        // Get start and end address
        val startAddress = getAddressFromLatLng(
            start.latitude,
            start.longitude
        ) ?: "Unknown Location"

        val endAddress = getAddressFromLatLng(
            end.latitude,
            end.longitude
        ) ?: "Unknown Location"

        // Get stay points
        val stayPoints = snapshot.child("stayPoints")
            .children.mapNotNull {
                it.getValue(StayPoint::class.java)
            }

        val sessionId = "${userId}_${startedAt}"

        // Save session history
        val sessionHistory = SessionHistory(
            id = sessionId,
            userId = userId,
            userName = userName,
            startLat = start.latitude,
            startLng = start.longitude,
            endLat = end.latitude,
            endLng = end.longitude,
            startTime = startedAt,
            endTime = System.currentTimeMillis(),
            startAddress = startAddress,
            endAddress = endAddress,
            userPath = pathPoints,
            sharedWith = sharedWith,
            stayPoints = stayPoints
        )

        rootRef.child("sessions")
            .child(userId)
            .child(sessionId)
            .setValue(sessionHistory)
            .await()

        liveRef.removeValue().await()
    }

    // Get address from latlng using Geocoder
    private suspend fun getAddressFromLatLng(
        lat: Double,
        lng: Double
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(
                    context,
                    Locale.getDefault()
                )

                val address = geocoder.getFromLocation(lat, lng, 1)
                address?.firstOrNull()?.getAddressLine(0)
            } catch (e: Exception) {
                Log.e("GEOCODER", "Error: ${e.message}")
                null
            }
        }
    }

    // Save stay point
    override suspend fun saveStayPoint(
        userId: String,
        stayPoint: StayPoint
    ) {
        rootRef.child("live_location")
            .child(userId)
            .child("stayPoints")
            .push()
            .setValue(stayPoint)
            .await()
    }

    // Check if user is sharing live location
    override suspend fun isLiveLocationActive(userId: String): Boolean {
        val snapshot = rootRef
            .child("live_location")
            .child(userId)
            .child("active")
            .get()
            .await()

        return snapshot.getValue(Boolean::class.java) == true
    }

    // Get timestamp for last location update
    override fun getUpdatedTimestamp(userId: String): Flow<Long> = callbackFlow {
        val ref = rootRef.child("live_location")
            .child(userId)
            .child("timestamp")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val timestamp = snapshot.getValue(Long::class.java)
                timestamp?.let { trySend(it) }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }

        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

}