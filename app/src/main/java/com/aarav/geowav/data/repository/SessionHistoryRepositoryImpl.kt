package com.aarav.geowav.data.repository

import android.util.Log
import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.core.utils.rangeForFilter
import com.aarav.geowav.data.mapper.FirebaseActivity
import com.aarav.geowav.data.mapper.toGeoAlert
import com.aarav.geowav.data.model.GeoAlert
import com.aarav.geowav.data.model.SessionHistory
import com.aarav.geowav.data.model.TimelineItem
import com.aarav.geowav.data.model.toTimelineItem
import com.aarav.geowav.domain.repository.SessionHistoryRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.sql.Time

class SessionHistoryRepositoryImpl
@Inject constructor(
    val firebaseDatabase: FirebaseDatabase
) : SessionHistoryRepository {

    val rootRef = firebaseDatabase.reference

    override suspend fun saveSession(session: SessionHistory) {
        val sessionRef = rootRef.child("sessions")
            .child(session.userId)
            .child(session.id)

        sessionRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                if (currentData.value != null) {
                    return Transaction.abort()
                }
                currentData.value = session
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    Log.e("SessionHistoryRepo", "Transaction failed: ${error.message}")
                } else if (committed) {
                    Log.d("SessionHistoryRepo", "Session saved successfully")
                } else {
                    Log.w("SessionHistoryRepo", "Transaction aborted - session likely already exists")
                }
            }
        })
    }

    override fun getSessionsVisibleTo(
        ownerId: String,
        viewerId: String,
        filter: ActivityFilter
    ): Flow<List<TimelineItem>> = callbackFlow {


        val timeRange = rangeForFilter(filter)

        val ref = rootRef.child("sessions")
            .child(ownerId)
            .orderByChild("startTime")
            .startAt(timeRange.startMillis.toDouble())
            .endAt(timeRange.endMillis.toDouble())


        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val sessions = snapshot.children.mapNotNull { child ->

                    val session = child.getValue(SessionHistory::class.java)

                    //Log.i("SESSION", "other: " + session.toString())
                    if (session != null &&
                        session.sharedWith.contains(viewerId)
                    ) {

                        session.toTimelineItem(session.userName)
                    } else {
                        null
                    }
                }
                    .sortedByDescending { it.startTime }

                trySend(sessions)
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

    override fun getSessionsForCurrentUser(
        userId: String,
        filter: ActivityFilter
    ): Flow<List<TimelineItem>> = callbackFlow {

        val timeRange = rangeForFilter(filter)

        val ref = rootRef.child("sessions")
            .child(userId)
            .orderByChild("startTime")
            .startAt(timeRange.startMillis.toDouble())
            .endAt(timeRange.endMillis.toDouble())


        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                //Log.i("SESSION", "session: $snapshot")
                val sessions = snapshot.children.mapNotNull {
                    val session = it.getValue(SessionHistory::class.java)

                    session?.toTimelineItem(session.userName)
                }
                    .sortedByDescending { session ->
                        session.startTime


                    }
                //Log.i("SESSION", "data: $sessions")


                trySend(sessions)
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

    override suspend fun getSessionById(
        sessionId: String,
        userId: String
    ): TimelineItem? {
        val session = rootRef.child("sessions")
            .child(userId)
            .child(sessionId)
            .get()
            .await()
            .getValue(SessionHistory::class.java)

        //Log.i("SESSIONS", "session by id: " + session?.userPath.toString())

        return session?.toTimelineItem(session.userName)
    }

//    fun observeAlerts(filter: ActivityFilter): Flow<List<GeoAlert>> = callbackFlow{
//        val userID = uid()
//        val timeRange = rangeForFilter(filter)
//
//        val ref = db.getReference("geofence_activity")
//            .child(userID)
//
//        // Query for logs in the timestamp range
//        val query = ref
//            .orderByChild("timestamp")
//            .startAt(timeRange.startMillis.toDouble())
//            .endAt(timeRange.endMillis.toDouble())
//
//        val listener = object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val alerts = snapshot.children.mapNotNull { snap ->
//                    val activity = snap.getValue(FirebaseActivity::class.java)
//                    activity?.toGeoAlert(id = snap.key ?: "")
//                }.sortedByDescending { alert ->
//                    // If you later add timestamp to GeoAlert, sort by that.
//                    // For now, we just keep the order from Firebase (usually already by timestamp).
//                    alert.time // not perfect, but okay if readableTime is ordered
//                }
//
//                trySend(alerts)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                close(error.toException())
//            }
//        }
//
//
//        query.addValueEventListener(listener)
//        awaitClose { query.removeEventListener(listener) }
//
//    }
}