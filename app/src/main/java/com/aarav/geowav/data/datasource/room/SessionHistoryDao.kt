package com.aarav.geowav.data.datasource.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aarav.geowav.data.model.SessionHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionHistory)

    @Query("SELECT * FROM session_history WHERE userId = :userId ORDER BY startTime DESC")
    fun getSessionsForUser(userId: String): Flow<List<SessionHistory>>

    @Query("SELECT * FROM session_history WHERE id = :sessionId")
    fun getSessionById(sessionId: String): Flow<SessionHistory>

}