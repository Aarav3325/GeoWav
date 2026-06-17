package com.aarav.geowav.domain.repository

import com.aarav.geowav.data.model.DayReplay
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DayReplayRepository {
    fun observeDayReplays(): Flow<Map<LocalDate, DayReplay>>
}
