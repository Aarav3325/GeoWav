package com.aarav.geowav.core.utils

import java.time.LocalDate

sealed class ActivityFilter{
    object Today: ActivityFilter()
    object Yesterday: ActivityFilter()
    object Last7Days: ActivityFilter()
    data class Between(val from: LocalDate, val to: LocalDate): ActivityFilter()
}