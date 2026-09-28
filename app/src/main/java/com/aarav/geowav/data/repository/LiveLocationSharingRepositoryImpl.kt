package com.aarav.geowav.data.repository

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.aarav.geowav.data.model.DestinationLocation
import com.aarav.geowav.data.model.LocationUpdates
import com.aarav.geowav.data.model.SessionHistory
import com.aarav.geowav.data.model.SessionMode
import com.aarav.geowav.data.model.SessionStatus
import com.aarav.geowav.data.model.SharingSession
import com.aarav.geowav.data.model.StayPoint
import com.aarav.geowav.data.model.UserPathLatLng
import com.aarav.geowav.data.model.canTransitionTo
import com.aarav.geowav.domain.repository.LiveLocationSharingRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
        userName: String,
        userId: String,
        lat: Double,
        long: Double,
        mode: SessionMode,
        expiresAt: Long?,
        destinationPlaceId: String?,
        destinationLocation: DestinationLocation?,
        createdFrom: String?
    ) {
        val now = System.currentTimeMillis()
        val sessionId = "${userId}_${now}"

        val update = hashMapOf<String, Any>()
        val pathRef = rootRef.child("live_location/$userId").child("path").push()

        update["live_location/$userId/sessionId"] = sessionId
        update["live_location/$userId/ownerId"] = userId
        update["live_location/$userId/lat"] = lat
        update["live_location/$userId/lng"] = long
        update["live_location/$userId/timestamp"] = now
        update["live_location/$userId/active"] = true
        update["live_location/$userId/startedAt"] = now
        update["live_location/$userId/userName"] = userName
        update["live_location/$userId/mode"] = mode.name
        update["live_location/$userId/status"] = SessionStatus.ACTIVE.name

        expiresAt?.let { update["live_location/$userId/expiresAt"] = it }
        destinationPlaceId?.let { update["live_location/$userId/destinationPlaceId"] = it }
        destinationLocation?.let {
            update["live_location/$userId/destinationLocation"] = mapOf(
                "latitude" to it.latitude,
                "longitude" to it.longitude,
                "name" to it.name,
                "address" to it.address
            )
            update["live_location/$userId/destinationName"] = it.name
        }
        createdFrom?.let { update["live_location/$userId/createdFrom"] = it }

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

    override suspend fun updateLocation(userId: String, lat: Double, long: Double) {
        val pathRef = rootRef.child("live_location/$userId").child("path").push()
        val now = System.currentTimeMillis()
        val updates = hashMapOf<String, Any>()

        updates["live_location/$userId/lat"] = lat
        updates["live_location/$userId/lng"] = long
        updates["live_location/$userId/timestamp"] = now

        updates["live_location/$userId/path/${pathRef.key}"] = mapOf(
            "lat" to lat,
            "lng" to long,
            "timestamp" to now
        )

        rootRef.updateChildren(updates).await()
    }

    override suspend fun updateSessionStatus(userId: String, newStatus: SessionStatus): Boolean {
        val ref = rootRef.child("live_location").child(userId)
        val snapshot = ref.get().await()
        if (!snapshot.exists()) return false

        val currentStatusStr = snapshot.child("status").getValue(String::class.java) ?: SessionStatus.ACTIVE.name
        val currentStatus = try { SessionStatus.valueOf(currentStatusStr) } catch (_: Exception) { SessionStatus.ACTIVE }

        if (!currentStatus.canTransitionTo(newStatus)) {
            Log.w("Session2.0", "Invalid status transition from $currentStatus to $newStatus for user $userId")
            return false
        }

        if (currentStatus == newStatus) return true

        ref.child("status").setValue(newStatus.name).await()
        return true
    }

    override fun observeActiveSession(userId: String): Flow<SharingSession?> = callbackFlow {
        val ref = rootRef.child("live_location").child(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(null)
                    return
                }

                val active = snapshot.child("active").getValue(Boolean::class.java) == true
                if (!active) {
                    trySend(null)
                    return
                }

                val lat = snapshot.child("lat").getValue(Double::class.java) ?: 0.0
                val lng = snapshot.child("lng").getValue(Double::class.java) ?: 0.0
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                val startedAt = snapshot.child("startedAt").getValue(Long::class.java) ?: 0L
                val userName = snapshot.child("userName").getValue(String::class.java) ?: ""
                val modeStr = snapshot.child("mode").getValue(String::class.java) ?: SessionMode.NORMAL.name
                val statusStr = snapshot.child("status").getValue(String::class.java) ?: SessionStatus.ACTIVE.name
                val expiresAt = snapshot.child("expiresAt").getValue(Long::class.java)
                val destPlaceId = snapshot.child("destinationPlaceId").getValue(String::class.java)
                val destSnap = snapshot.child("destinationLocation")
                val destLocation = if (destSnap.exists()) {
                    DestinationLocation(
                        latitude = destSnap.child("latitude").getValue(Double::class.java) ?: 0.0,
                        longitude = destSnap.child("longitude").getValue(Double::class.java) ?: 0.0,
                        name = destSnap.child("name").getValue(String::class.java) ?: "",
                        address = destSnap.child("address").getValue(String::class.java) ?: ""
                    )
                } else null
                val createdFrom = snapshot.child("createdFrom").getValue(String::class.java)
                val sharedWith = snapshot.child("sharedWith").children.mapNotNull { it.getValue(String::class.java) }
                val sessionId = snapshot.child("sessionId").getValue(String::class.java) ?: "${userId}_${startedAt}"

                val mode = try { SessionMode.valueOf(modeStr) } catch (_: Exception) { SessionMode.NORMAL }
                val status = try { SessionStatus.valueOf(statusStr) } catch (_: Exception) { SessionStatus.ACTIVE }

                val session = SharingSession(
                    sessionId = sessionId,
                    ownerId = userId,
                    ownerName = userName,
                    mode = mode,
                    status = status,
                    startedAt = startedAt,
                    expiresAt = expiresAt,
                    destinationPlaceId = destPlaceId,
                    destinationLocation = destLocation,
                    sharedWith = sharedWith,
                    createdFrom = createdFrom,
                    active = true,
                    lat = lat,
                    lng = lng,
                    timestamp = timestamp
                )
                trySend(session)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun stopSharingLiveLocation(userId: String, finalStatus: SessionStatus) {
        val liveRef = rootRef.child("live_location").child(userId)
        val snapshot = liveRef.get().await()

        if (!snapshot.exists()) return

        if (snapshot.child("path").childrenCount < 2) {
            liveRef.removeValue().await()
            return
        }

        val startedAt = snapshot.child("startedAt")
            .getValue(Long::class.java) ?: return

        val userName = snapshot.child("userName")
            .getValue(String::class.java) ?: ""

        val modeStr = snapshot.child("mode").getValue(String::class.java) ?: SessionMode.NORMAL.name
        val mode = try { SessionMode.valueOf(modeStr) } catch (_: Exception) { SessionMode.NORMAL }

        val expiresAt = snapshot.child("expiresAt").getValue(Long::class.java)
        val destPlaceId = snapshot.child("destinationPlaceId").getValue(String::class.java)
        val destSnap = snapshot.child("destinationLocation")
        val destLocation = if (destSnap.exists()) {
            DestinationLocation(
                latitude = destSnap.child("latitude").getValue(Double::class.java) ?: 0.0,
                longitude = destSnap.child("longitude").getValue(Double::class.java) ?: 0.0,
                name = destSnap.child("name").getValue(String::class.java) ?: "",
                address = destSnap.child("address").getValue(String::class.java) ?: ""
            )
        } else null
        val createdFrom = snapshot.child("createdFrom").getValue(String::class.java)

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

        val startAddress = getAddressFromLatLng(
            start.latitude,
            start.longitude
        ) ?: "Unknown Location"

        val endAddress = getAddressFromLatLng(
            end.latitude,
            end.longitude
        ) ?: "Unknown Location"

        val stayPoints = snapshot.child("stayPoints")
            .children.mapNotNull {
                it.getValue(StayPoint::class.java)
            }

        val existingSessionId = snapshot.child("sessionId").getValue(String::class.java)
        val sessionId = existingSessionId ?: "${userId}_${startedAt}"

        val sessionHistory = SessionHistory(
            id = sessionId,
            userId = userId,
            userName = userName,
            mode = mode,
            status = finalStatus,
            expiresAt = expiresAt,
            destinationPlaceId = destPlaceId,
            destinationLocation = destLocation,
            createdFrom = createdFrom,
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

    override suspend fun isLiveLocationActive(userId: String): Boolean {
        val snapshot = rootRef
            .child("live_location")
            .child(userId)
            .child("active")
            .get()
            .await()

        return snapshot.getValue(Boolean::class.java) == true
    }

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

    override fun observeSharingActive(userId: String): Flow<Boolean> = callbackFlow {
        val ref = rootRef.child("live_location").child(userId).child("active")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val active = snapshot.getValue(Boolean::class.java) == true
                trySend(active)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

}