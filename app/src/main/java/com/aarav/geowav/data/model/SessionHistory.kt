package com.aarav.geowav.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_history")
data class SessionHistory(
    @PrimaryKey
    val id: String,
    val userId: String,
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val startTime: Long,
    val endTime: Long,
    val startAddress: String,
    val endAddress: String
)
