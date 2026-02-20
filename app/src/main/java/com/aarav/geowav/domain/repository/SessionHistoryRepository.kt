package com.aarav.geowav.domain.repository

import com.aarav.geowav.data.model.SessionHistory
import com.aarav.geowav.data.model.TimelineItem
import kotlinx.coroutines.flow.Flow

interface SessionHistoryRepository {

    suspend fun saveSession(session: SessionHistory)

    fun getSessionsForUser(userId: String): Flow<List<SessionHistory>>

    fun getSessionById(sessionId: String): Flow<SessionHistory>

    suspend fun saveSessionFirebase(session: SessionHistory)

    fun getSessionForUserFirebase(userId: String): Flow<List<TimelineItem>>

    suspend fun getSessionByIdFirebase(sessionId: String, userId: String): TimelineItem?

}