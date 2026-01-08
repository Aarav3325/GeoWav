package com.aarav.geowav.data.repository

import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.data.mapper.FirebaseActivity
import com.aarav.geowav.data.mapper.toGeoAlert
import com.aarav.geowav.data.model.GeoAlert
import com.aarav.geowav.domain.repository.GeoActivityRepository
import com.aarav.geowav.core.utils.rangeForFilter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class GeoActivityRepositoryImpl
@Inject constructor(
    private val db: FirebaseDatabase,
    private val auth: FirebaseAuth
) : GeoActivityRepository {

    private fun uid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

    override fun observeAlerts(filter: ActivityFilter): Flow<List<GeoAlert>> = callbackFlow{
        val userID = uid()
        val timeRange = rangeForFilter(filter)

        val ref = db.getReference("geofence_activity")
            .child(userID)

        // Query for logs in the timestamp range
        val query = ref
            .orderByChild("timestamp")
            .startAt(timeRange.startMillis.toDouble())
            .endAt(timeRange.endMillis.toDouble())

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alerts = snapshot.children.mapNotNull { snap ->
                    val activity = snap.getValue(FirebaseActivity::class.java)
                    activity?.toGeoAlert(id = snap.key ?: "")
                }.sortedByDescending { alert ->
                    // If you later add timestamp to GeoAlert, sort by that.
                    // For now, we just keep the order from Firebase (usually already by timestamp).
                    alert.time // not perfect, but okay if readableTime is ordered
                }

                trySend(alerts)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }


        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }

    }

}