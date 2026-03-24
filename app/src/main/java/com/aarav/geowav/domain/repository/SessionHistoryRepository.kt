package com.aarav.geowav.domain.repository

import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.data.model.SessionHistory
import com.aarav.geowav.data.model.TimelineItem
import com.aarav.geowav.data.model.UserPlan
import kotlinx.coroutines.flow.Flow

interface SessionHistoryRepository {

    suspend fun saveSession(session: SessionHistory)

    fun getSessionsVisibleTo (
        ownerId: String,
        viewerId: String,
        filter: ActivityFilter,
        plan: UserPlan
    ): Flow<List<TimelineItem>>

    fun getSessionsForCurrentUser (
        userId: String,
        filter: ActivityFilter,
        plan: UserPlan
    ): Flow<List<TimelineItem>>

    suspend fun getSessionById(sessionId: String, userId: String): TimelineItem?

}