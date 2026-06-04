package com.aarav.geowav.data.repository

import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.core.insights.AverageVisitDurationInsight
import com.aarav.geowav.core.insights.MostVisitedPlaceInsight
import com.aarav.geowav.core.insights.PersonalInsightScope
import com.aarav.geowav.core.insights.averageVisitDurationInsight
import com.aarav.geowav.core.insights.mostVisitedPlaceInsight
import com.aarav.geowav.core.insights.rangeForPersonalInsightScope
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
import com.aarav.geowav.data.authentication.GoogleSignInClient

class GeoActivityRepositoryImpl
@Inject constructor(
    private val db: FirebaseDatabase,
    private val auth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient
) : GeoActivityRepository {

    private fun uid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

    override fun observeAlerts(filter: ActivityFilter): Flow<List<GeoAlert>> = callbackFlow {
        val userID = uid()
        val timeRange = rangeForFilter(filter)

        val ref = db.getReference("geofence_activity")
            .child(userID)

        val username = googleSignInClient.getUserName()

        val query = ref
            .orderByChild("timestamp")
            .startAt(timeRange.startMillis.toDouble())
            .endAt(timeRange.endMillis.toDouble())

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alerts = snapshot.children.mapNotNull { snap ->
                    val activity = snap.getValue(FirebaseActivity::class.java)
                    activity?.toGeoAlert(id = snap.key ?: "", username = username)
                }.sortedByDescending { alert ->
                    alert.timestamp
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

    override fun observeMostVisitedPlace(
        scope: PersonalInsightScope
    ): Flow<MostVisitedPlaceInsight?> = callbackFlow {
        val userID = uid()
        val (startMillis, endMillis) = rangeForPersonalInsightScope(scope)

        val query = db.getReference("geofence_activity")
            .child(userID)
            .orderByChild("timestamp")
            .startAt(startMillis.toDouble())
            .endAt(endMillis.toDouble())

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val activities = snapshot.children.mapNotNull { snap ->
                    snap.getValue(FirebaseActivity::class.java)
                }

                trySend(mostVisitedPlaceInsight(activities, scope))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    override fun observeAverageVisitDuration(
        scope: PersonalInsightScope
    ): Flow<AverageVisitDurationInsight?> = callbackFlow {
        val userID = uid()
        val (startMillis, endMillis) = rangeForPersonalInsightScope(scope)

        val query = db.getReference("geofence_activity")
            .child(userID)
            .orderByChild("timestamp")
            .startAt(startMillis.toDouble())
            .endAt(endMillis.toDouble())

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val activities = snapshot.children.mapNotNull { snap ->
                    snap.getValue(FirebaseActivity::class.java)
                }

                trySend(averageVisitDurationInsight(activities, scope))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

}
