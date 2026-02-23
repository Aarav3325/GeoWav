package com.aarav.geowav.data.repository

import android.util.Log
import com.aarav.geowav.data.model.SessionHistory
import com.aarav.geowav.data.model.TimelineItem
import com.aarav.geowav.data.model.toTimelineItem
import com.aarav.geowav.domain.repository.SessionHistoryRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
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
        rootRef.child("sessions")
            .child(session.userId)
            .child(session.id)
            .setValue(session)
            .await()
    }

    override fun getSessionsForUser(
        ownerId: String,
        viewerId: String
    ): Flow<List<TimelineItem>> = callbackFlow {

        val ref = rootRef.child("sessions")
            .child(ownerId)

        Log.i("SESSION", "allowed")

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val sessions = snapshot.children.mapNotNull { child ->

                    val session = child.getValue(SessionHistory::class.java)

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

        Log.i("SESSIONS", "session by id: " + session?.userPath.toString())

        return session?.toTimelineItem(session.userName)
    }
}