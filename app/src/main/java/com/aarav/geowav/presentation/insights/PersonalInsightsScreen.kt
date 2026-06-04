package com.aarav.geowav.presentation.insights

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            InsightsIntroCard(
                selectedScope = uiState.mostVisitedPlaceScope
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
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(2) {
                            SkeletonMetricCard()
                        }
                    }
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
                    averageVisitDurationInsight?.let { insight ->
                        AverageVisitDurationInsightCard(
                            insight = insight,
                            selectedScope = uiState.mostVisitedPlaceScope
                        )
                    }

                    mostVisitedPlaceInsight?.let { insight ->
                        MostVisitedPlaceInsightCard(
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
private fun InsightsIntroCard(
    selectedScope: PersonalInsightScope
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Your place rhythm",
                fontFamily = manrope,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (selectedScope == PersonalInsightScope.Month) {
                    "A quiet look at the places that shaped this month."
                } else {
                    "A quiet look at the places that shaped this week."
                },
                fontFamily = manrope,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
            )
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
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        PersonalInsightScopeChip(
            label = "Week",
            selected = selectedScope == PersonalInsightScope.Week,
            onClick = { onScopeSelected(PersonalInsightScope.Week) },
            modifier = Modifier.weight(1f)
        )
        PersonalInsightScopeChip(
            label = "Month",
            selected = selectedScope == PersonalInsightScope.Month,
            onClick = { onScopeSelected(PersonalInsightScope.Month) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MostVisitedPlaceInsightCard(
    insight: MostVisitedPlaceInsight,
    selectedScope: PersonalInsightScope,
    modifier: Modifier = Modifier
) {
    InsightMetricCard(
        title = "Most Visited Place",
        selectedScope = selectedScope,
        primaryText = insight.placeName,
        supportingText = "${insight.visitCount} ${if (insight.visitCount == 1) "visit" else "visits"} ${
            if (selectedScope == PersonalInsightScope.Month) {
                "this month"
            } else {
                "this week"
            }
        }",
        accentColor = MaterialTheme.colorScheme.tertiary,
        modifier = modifier
    )
}

@Composable
private fun AverageVisitDurationInsightCard(
    insight: AverageVisitDurationInsight,
    selectedScope: PersonalInsightScope,
    modifier: Modifier = Modifier
) {
    InsightMetricCard(
        title = "Average Visit Duration",
        selectedScope = selectedScope,
        primaryText = formatDuration(insight.averageDurationMillis),
        supportingText = "Typical time at ${insight.placeName}",
        accentColor = MaterialTheme.colorScheme.secondary,
        modifier = modifier
    )
}

@Composable
private fun InsightMetricCard(
    title: String,
    selectedScope: PersonalInsightScope,
    primaryText: String,
    supportingText: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(50))
                    .background(accentColor.copy(alpha = 0.18f))
                    .padding(horizontal = 3.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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

                Text(
                    text = primaryText,
                    fontFamily = manrope,
                    fontSize = 26.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = supportingText,
                    fontFamily = manrope,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            fontFamily = manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SkeletonMetricCard() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 800
                0.35f at 0
                0.65f at 400
                0.35f at 800
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .height(42.dp)
                    .width(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.12f))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.08f))
                    )
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.08f))
                    )
                }

                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(26.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.08f))
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.08f))
                )
            }
        }
    }
}
