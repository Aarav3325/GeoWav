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

        var currentMode: ShareMode = ShareMode.Blocked

        val locationRef = rootRef
            .child("live_location")
            .child(userId)

        val locationListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val location = snapshot.getValue(LocationUpdates::class.java) ?: return

                when (val mode = currentMode) {
                    is ShareMode.Emergency -> {

                        trySend(
                            ViewerLocationState.EmergencySharing(
                                location = location,
                                endsAt = mode.endsAt
                            )
                        )
                    }

                    ShareMode.Normal -> {

                        trySend(
                            ViewerLocationState.NormalSharing(
                                location
                            )
                        )
                    }

                    ShareMode.Blocked -> Unit
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        locationRef.addValueEventListener(locationListener)

        val emergencyRef = rootRef
            .child("emergency_sharing")
            .child(userId)

        val emergencyListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val active = snapshot.child("active").getValue(Boolean::class.java) == true
                val endsAt = snapshot.child("endsAt").getValue(Long::class.java)
                val allowed = snapshot
                    .child("viewers")
                    .child(viewerId)
                    .getValue(Boolean::class.java) == true

                if(active && allowed && endsAt != null && System.currentTimeMillis() < endsAt){
                    currentMode = ShareMode.Emergency(endsAt)
                    return
                }

                currentMode = ShareMode.Blocked
                trySend(ViewerLocationState.Blocked)

                rootRef.child("location_sharing")
                    .child(userId)
                    .child(viewerId)
                    .get()
                    .addOnSuccessListener {
                        snap ->
                        if(snap.getValue(Boolean::class.java) == true) {
                            currentMode = ShareMode.Normal
                        }
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }

        }

        emergencyRef.addValueEventListener(emergencyListener)

        awaitClose {
            locationRef.removeEventListener(locationListener)
            emergencyRef.removeEventListener(emergencyListener)
        }

    }
}