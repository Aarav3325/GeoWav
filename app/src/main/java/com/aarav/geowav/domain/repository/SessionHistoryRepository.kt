package com.aarav.geowav.domain.repository

import com.aarav.geowav.data.model.SessionHistory
import kotlinx.coroutines.flow.Flow

interface SessionHistoryRepository {

    suspend fun insertSession(session: SessionHistory)

    fun getSessionsForUser(userId: String): Flow<List<SessionHistory>>
}