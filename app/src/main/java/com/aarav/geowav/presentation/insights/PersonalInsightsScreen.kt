package com.aarav.geowav.presentation.insights

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.core.insights.AverageVisitDurationInsight
import com.aarav.geowav.core.insights.MostVisitedPlaceInsight
import com.aarav.geowav.core.insights.PersonalInsightScope
import com.aarav.geowav.presentation.theme.manrope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInsightsScreen(
    viewModel: PersonalInsightsViewModel,
    back: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Text(
                        text = "Insights",
                        fontSize = 20.sp,
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = "back arrow",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val mostVisitedPlaceInsight = uiState.mostVisitedPlaceInsight
        val averageVisitDurationInsight = uiState.averageVisitDurationInsight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Small reflections from your place awareness history.",
                fontFamily = manrope,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            InsightScopeSelector(
                selectedScope = uiState.mostVisitedPlaceScope,
                onScopeSelected = viewModel::onScopeChanged
            )

            when {
                uiState.error != null -> {
                    Text(
                        text = "Insights are unavailable right now.",
                        fontFamily = manrope,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                uiState.isLoading -> {
                    Text(
                        text = "Reading your place history...",
                        fontFamily = manrope,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                mostVisitedPlaceInsight == null && averageVisitDurationInsight == null -> {
                    Text(
                        text = "No insights yet",
                        fontFamily = manrope,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "GeoWav will reflect your patterns after arrivals are recorded.",
                        fontFamily = manrope,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    mostVisitedPlaceInsight?.let { insight ->
                        MostVisitedPlaceInsightCard(
                            insight = insight,
                            selectedScope = uiState.mostVisitedPlaceScope
                        )
                    }

                    averageVisitDurationInsight?.let { insight ->
                        AverageVisitDurationInsightCard(
                            insight = insight,
                            selectedScope = uiState.mostVisitedPlaceScope
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InsightScopeSelector(
    selectedScope: PersonalInsightScope,
    onScopeSelected: (PersonalInsightScope) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        PersonalInsightScopeChip(
            label = "Week",
            selected = selectedScope == PersonalInsightScope.Week,
            onClick = { onScopeSelected(PersonalInsightScope.Week) }
        )
        PersonalInsightScopeChip(
            label = "Month",
            selected = selectedScope == PersonalInsightScope.Month,
            onClick = { onScopeSelected(PersonalInsightScope.Month) }
        )
    }
}

@Composable
private fun MostVisitedPlaceInsightCard(
    insight: MostVisitedPlaceInsight,
    selectedScope: PersonalInsightScope,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InsightCardHeader(
                title = "Most Visited Place",
                selectedScope = selectedScope
            )

            Text(
                text = insight.placeName,
                fontFamily = manrope,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${insight.visitCount} ${if (insight.visitCount == 1) "visit" else "visits"} ${
                    if (selectedScope == PersonalInsightScope.Month) {
                        "this month"
                    } else {
                        "this week"
                    }
                }",
                fontFamily = manrope,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AverageVisitDurationInsightCard(
    insight: AverageVisitDurationInsight,
    selectedScope: PersonalInsightScope,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InsightCardHeader(
                title = "Average Visit Duration",
                selectedScope = selectedScope
            )

            Text(
                text = insight.placeName,
                fontFamily = manrope,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatDuration(insight.averageDurationMillis),
                fontFamily = manrope,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InsightCardHeader(
    title: String,
    selectedScope: PersonalInsightScope
) {
    Column {
        Text(
            text = title,
            fontFamily = manrope,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (selectedScope == PersonalInsightScope.Month) {
                "This month"
            } else {
                "This week"
            },
            fontFamily = manrope,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
        )
    }
}

private fun formatDuration(durationMillis: Long): String {
    val totalMinutes = durationMillis / 60_000L
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L

    return when {
        hours > 0L && minutes > 0L -> "${hours}h ${minutes}m"
        hours > 0L -> "${hours}h"
        else -> "${minutes}m"
    }
}

@Composable
private fun PersonalInsightScopeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        shape = RoundedCornerShape(50),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontFamily = manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )
    }
}
