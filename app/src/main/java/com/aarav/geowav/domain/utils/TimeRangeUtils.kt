package com.aarav.geowav.domain.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.aarav.geowav.data.geofence.ActivityFilter
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val indiaZone: ZoneId = ZoneId.of("Asia/Kolkata")
private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(indiaZone)

fun LocalDate.startOfDayMillis(): Long = atStartOfDay(indiaZone).toInstant().toEpochMilli()

fun LocalDate.endOfDayMillis(): Long =
    this.plusDays(1).atStartOfDay(indiaZone).toInstant().toEpochMilli() - 1

data class TimeRange(val startMillis: Long, val endMillis: Long)

fun rangeForFilter(filter: ActivityFilter): TimeRange {
    val today = LocalDate.now(indiaZone)

    return when (filter) {
        ActivityFilter.Today -> {
            TimeRange(today.startOfDayMillis(), today.endOfDayMillis())
        }

        ActivityFilter.Yesterday -> {
            val yes = today.minusDays(1)
            TimeRange(yes.startOfDayMillis(), yes.endOfDayMillis())
        }

        ActivityFilter.Last7Days -> {
            val sevenDaysAgo = today.minusDays(6)
            TimeRange(sevenDaysAgo.startOfDayMillis(), today.endOfDayMillis())
        }

        is ActivityFilter.Between ->{
            TimeRange(filter.from.startOfDayMillis(), filter.to.endOfDayMillis())
        }
    }
}