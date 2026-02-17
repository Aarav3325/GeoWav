package com.aarav.geowav.data.repository

import android.util.Log
import com.aarav.geowav.core.utils.ShareMode
import com.aarav.geowav.core.utils.ViewerLocationState
import com.aarav.geowav.data.model.LocationUpdates
import com.aarav.geowav.domain.repository.ViewerLocationRepository
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

        val rootRef = firebaseDatabase.reference

    override fun observeUserLocation(
        userId: String,
        viewerId: String
    ): Flow<ViewerLocationState> = callbackFlow {

        var latestLocation: LocationUpdates? = null
        var isEmergencyActive = false
        var emergencyEndsAt: Long? = null
        var isNormalAllowed = false

        fun emitState() {
            val state = when {
                isEmergencyActive && emergencyEndsAt != null ->
                    ViewerLocationState.EmergencySharing(
                        location = latestLocation ?: return,
                        endsAt = emergencyEndsAt!!
                    )

                isNormalAllowed && latestLocation != null ->
                    ViewerLocationState.NormalSharing(latestLocation!!)

                else ->
                    ViewerLocationState.Blocked
            }

            trySend(state)
        }

        val locationRef = rootRef.child("live_location").child(userId)
        val emergencyRef = rootRef.child("emergency_sharing").child(userId)
        val normalRef = rootRef.child("location_sharing").child(userId).child(viewerId)

        val locationListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                latestLocation = snapshot.getValue(LocationUpdates::class.java)
                emitState()
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val emergencyListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val active = snapshot.child("active").getValue(Boolean::class.java) == true
                val endsAt = snapshot.child("endsAt").getValue(Long::class.java)
                val allowed = snapshot.child("viewers")
                    .child(viewerId)
                    .getValue(Boolean::class.java) == true

                if (active && allowed && endsAt != null && System.currentTimeMillis() < endsAt) {
                    isEmergencyActive = true
                    emergencyEndsAt = endsAt
                } else {
                    isEmergencyActive = false
                    emergencyEndsAt = null
                }

                emitState()
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val normalListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isNormalAllowed = snapshot.getValue(Boolean::class.java) == true
                emitState()
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        locationRef.addValueEventListener(locationListener)
        emergencyRef.addValueEventListener(emergencyListener)
        normalRef.addValueEventListener(normalListener)

        awaitClose {
            locationRef.removeEventListener(locationListener)
            emergencyRef.removeEventListener(emergencyListener)
            normalRef.removeEventListener(normalListener)
        }
    }


}
