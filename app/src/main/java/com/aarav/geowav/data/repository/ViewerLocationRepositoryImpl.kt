package com.aarav.geowav.data.repository

import com.aarav.geowav.core.utils.ViewerLocationState
import com.aarav.geowav.data.model.LocationUpdates
import com.aarav.geowav.data.model.StayPoint
import com.aarav.geowav.domain.repository.ViewerLocationRepository
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ViewerLocationRepositoryImpl
@Inject constructor(
    val firebaseDatabase: FirebaseDatabase
) : ViewerLocationRepository {
    private val rootRef = firebaseDatabase.reference

    override fun observeUserLocation(
        userId: String,
        viewerId: String
    ): Flow<ViewerLocationState> = callbackFlow {

        val locationRef = rootRef.child("live_location").child(userId)
        val emergencyRef = rootRef.child("emergency_sharing").child(userId)

        var isEmergencyActive = false
        var emergencyEndsAt: Long? = null
        var latestLocationSnapshot: DataSnapshot? = null

        fun parseAndEmit(snapshot: DataSnapshot) {
            latestLocationSnapshot = snapshot

            val active = snapshot.child("active")
                .getValue(Boolean::class.java) == true

            val lat = snapshot.child("lat").getValue(Double::class.java)
            val lng = snapshot.child("lng").getValue(Double::class.java)
            val timestamp = snapshot.child("timestamp").getValue(Long::class.java)
            val startedAt = snapshot.child("startedAt").getValue(Long::class.java)

            val sharedWith = snapshot.child("sharedWith")
                .children.mapNotNull { it.getValue(String::class.java) }

            val canViewLocation = sharedWith.contains(viewerId) || isEmergencyActive

            if (!active ||
                lat == null ||
                lng == null ||
                timestamp == null ||
                startedAt == null ||
                !canViewLocation
            ) {
                trySend(ViewerLocationState.Blocked)
                return
            }

            val location = LocationUpdates(
                lat = lat,
                lng = lng,
                timestamp = timestamp,
                startedAt = startedAt,
                sharedWith = sharedWith
            )

            val path = snapshot.child("path")
                .children.mapNotNull {
                    val pLat = it.child("lat").getValue(Double::class.java)
                    val pLng = it.child("lng").getValue(Double::class.java)
                    if (pLat != null && pLng != null)
                        LatLng(pLat, pLng)
                    else null
                }

            val stayPoints = snapshot.child("stayPoints")
                .children.mapNotNull {
                    it.getValue(StayPoint::class.java)
                }

            val state =
                if (isEmergencyActive && emergencyEndsAt != null) {
                    ViewerLocationState.EmergencySharing(
                        location = location,
                        endsAt = emergencyEndsAt!!,
                        path = path,
                        stayPoints = stayPoints
                    )
                } else {
                    ViewerLocationState.NormalSharing(
                        location = location,
                        path = path,
                        stayPoints = stayPoints
                    )
                }

            trySend(state)
        }

        val locationListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                parseAndEmit(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val emergencyListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val active = snapshot.child("active")
                    .getValue(Boolean::class.java) == true
                val endsAt = snapshot.child("endsAt")
                    .getValue(Long::class.java)
                val allowed = snapshot.child("viewers")
                    .child(viewerId)
                    .getValue(Boolean::class.java) == true

                if (active && allowed && endsAt != null &&
                    System.currentTimeMillis() < endsAt
                ) {
                    isEmergencyActive = true
                    emergencyEndsAt = endsAt
                } else {
                    isEmergencyActive = false
                    emergencyEndsAt = null
                }

                latestLocationSnapshot?.let { parseAndEmit(it) }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        locationRef.addValueEventListener(locationListener)
        emergencyRef.addValueEventListener(emergencyListener)

        awaitClose {
            locationRef.removeEventListener(locationListener)
            emergencyRef.removeEventListener(emergencyListener)
        }
    }

}
