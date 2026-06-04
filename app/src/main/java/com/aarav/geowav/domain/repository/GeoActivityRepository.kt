package com.aarav.geowav.domain.repository

import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.core.insights.AverageVisitDurationInsight
import com.aarav.geowav.core.insights.MostVisitedPlaceInsight
import com.aarav.geowav.core.insights.PersonalInsightScope
import com.aarav.geowav.data.model.GeoAlert
import kotlinx.coroutines.flow.Flow

interface GeoActivityRepository {
    fun observeAlerts(filter: ActivityFilter): Flow<List<GeoAlert>>
    fun observeMostVisitedPlace(scope: PersonalInsightScope): Flow<MostVisitedPlaceInsight?>
    fun observeAverageVisitDuration(scope: PersonalInsightScope): Flow<AverageVisitDurationInsight?>
}
