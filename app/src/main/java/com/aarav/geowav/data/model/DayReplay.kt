package com.aarav.geowav.data.model

import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant

enum class DayReplayTimeSection(val emoji: String, val label: String) {
    MORNING("☀", "Morning"),
    AFTERNOON("🌤", "Afternoon"),
    EVENING("🌇", "Evening"),
    NIGHT("🌙", "Night");

    companion object {
        fun fromTimestamp(timestamp: Long): DayReplayTimeSection {
            val hour = Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.of("Asia/Kolkata"))
                .hour
            return when (hour) {
                in 6..11 -> MORNING
                in 12..16 -> AFTERNOON
                in 17..20 -> EVENING
                else -> NIGHT
            }
        }
    }
}

data class DayReplayStop(
    val placeId: String,
    val placeName: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val radius: Float,
    val arrivedAt: Long,
    val departedAt: Long?,
    val stayDurationMillis: Long?
)

data class DayReplay(
    val date: LocalDate,
    val heroTitle: String,
    val heroSubtitle: String,
    val heroNarrative: String,
    val stops: List<DayReplayStop>
)

sealed class DayReplayUiItem {
    data class SectionHeader(val section: DayReplayTimeSection) : DayReplayUiItem()
    data class StopItem(val stop: DayReplayStop) : DayReplayUiItem()
}
