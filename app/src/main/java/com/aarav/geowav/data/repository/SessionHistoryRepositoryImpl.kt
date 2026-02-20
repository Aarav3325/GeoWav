package com.aarav.geowav.data.repository

import android.util.Log
import com.aarav.geowav.data.datasource.room.SessionHistoryDao
import com.aarav.geowav.data.model.SessionHistory
import com.aarav.geowav.data.model.TimelineItem
import com.aarav.geowav.data.model.toTimelineItem
import com.aarav.geowav.domain.repository.SessionHistoryRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.sql.Time
import javax.inject.Inject

class SessionHistoryRepositoryImpl
@Inject constructor(
    val sessionHistoryDao: SessionHistoryDao,
    val firebaseDatabase: FirebaseDatabase
) : SessionHistoryRepository {

    val rootRef = firebaseDatabase.reference

    override suspend fun saveSession(session: SessionHistory) {
        sessionHistoryDao.saveSession(session)
    }

    override fun getSessionsForUser(userId: String): Flow<List<SessionHistory>> {
        return sessionHistoryDao.getSessionsForUser(userId)
    }

    override fun getSessionById(sessionId: String): Flow<SessionHistory> {
        return sessionHistoryDao.getSessionById(sessionId)
    }

    override suspend fun saveSessionFirebase(session: SessionHistory) {
        rootRef.child("sessions")
            .child(session.userId)
            .child(session.id)
            .setValue(session)
            .await()
    }

    override fun getSessionForUserFirebase(userId: String): Flow<List<TimelineItem>> = callbackFlow {
        val ref = rootRef.child("sessions")
            .child(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                Log.i("SESSION", "session: $snapshot")
                val sessions = snapshot.children.mapNotNull {
                    val session = it.getValue(SessionHistory::class.java)

                    session?.toTimelineItem(session.userName)
                }
                    .sortedByDescending {
                        session ->
                        session.startTime
                    }
                Log.i("SESSION", "data: $sessions")
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

    override suspend fun getSessionByIdFirebase(
        sessionId: String,
        userId: String
    ): TimelineItem? {
        val session = rootRef.child("sessions")
            .child(userId)
            .child(sessionId)
            .get()
            .await()
            .getValue(SessionHistory::class.java)

        return session?.toTimelineItem(session.userName)
    }
}