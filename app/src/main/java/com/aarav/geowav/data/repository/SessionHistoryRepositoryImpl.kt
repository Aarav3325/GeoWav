package com.aarav.geowav.data.repository

import com.aarav.geowav.data.datasource.room.SessionHistoryDao
import com.aarav.geowav.data.model.SessionHistory
import com.aarav.geowav.domain.repository.SessionHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SessionHistoryRepositoryImpl
    @Inject constructor(
        val sessionHistoryDao: SessionHistoryDao
    ): SessionHistoryRepository {
    override suspend fun insertSession(session: SessionHistory) {
        sessionHistoryDao.insertSession(session)
    }

    override fun getSessionsForUser(userId: String): Flow<List<SessionHistory>> {
        return sessionHistoryDao.getSessionsForUser(userId)
    }

    override fun getSessionById(sessionId: String): Flow<SessionHistory> {
        return sessionHistoryDao.getSessionById(sessionId)
    }
}