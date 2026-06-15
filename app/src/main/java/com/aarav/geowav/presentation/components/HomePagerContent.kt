package com.aarav.geowav.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.core.insights.Insights
import com.aarav.geowav.core.insights.PersonalInsightScope
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.presentation.theme.GeoWavTheme
import com.aarav.geowav.presentation.theme.GeoWavThemeExtras


private val LiveGreen = Color(0xFF4CAF50)

private val EmergencyRed = Color(0xFFC62828)
private val EmergencyRedLight = Color(0xFFFFEBEE)
private val EmergencyAccentBar = Color(0xFFE53935)

private val NavyDeep = Color(0xFF222C61)
private val Periwinkle = Color(0xFFBAC3FF)

data class AwarenessSnapshotUiState(
    val currentPlace: String?,
    val isSharing: Boolean,
    val isEmergency: Boolean,
    val visibleMembers: List<CircleMember>,
    val latestActivity: LatestActivity?,
    val totalLovedOnesCount: Int
)

data class LatestActivity(
    val actorName: String,
    val actorAvatar: String?,
    val placeName: String,
    val isArrival: Boolean,
    val relativeTime: String
)

@Composable
fun AwarenessSnapshotCard(
    uiState: AwarenessSnapshotUiState,
    modifier: Modifier = Modifier
) {

    val topBarBrush = if (uiState.isEmergency) {
        listOf(EmergencyRed, EmergencyAccentBar)
    } else {
        listOf(NavyDeep, Periwinkle)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier,
        ) {
            GradientBar(topBarBrush)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                AwarenessCardLabel(uiState.isEmergency, uiState.isSharing)

                Spacer(Modifier.height(12.dp))

                Text(
                    text = uiState.currentPlace ?: "Away from saved places",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 26.sp,
                            letterSpacing = (-0.5).sp,
                            lineHeight = 30.sp,
                        )
                )

                Spacer(Modifier.height(8.dp))

                VisibilityMembers(uiState)

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )

                LatestActivitySection(
                    totalLovedOnesCount = uiState.totalLovedOnesCount,
                    latestActivity = uiState.latestActivity
                )
            }
        }
    }
}

@Composable
fun GradientBar(
    colors: List<Color>
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(Brush.horizontalGradient(colors))
    )
}

@Composable
fun SolidBar(
    solidColor: Color
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(solidColor)
    )
}

@Composable
fun AwarenessCardLabel(
    isEmergency: Boolean,
    isSharing: Boolean
) {

    val dotColor = when {
        isEmergency -> EmergencyAccentBar
        isSharing -> LiveGreen
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }

    val animatedDotColor by animateColorAsState(
        targetValue = dotColor,
        animationSpec = tween(400),
        label = "dotColor",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dotScale",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size((6 * dotScale).dp)
                .clip(CircleShape)
                .background(animatedDotColor),
        )

        Spacer(Modifier.width(6.dp))

        Text(
            text = "AWARENESS",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.08.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            ),
        )
    }
}

@Composable
private fun AvatarStack(
    members: List<CircleMember>
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy((-8).dp)
    ) {
        members.take(3).forEachIndexed { index, member ->
            val (avatarBg, avatarFg) = when (index % 3) {
                0 -> Pair(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer
                )

                1 -> Pair(
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.onSecondaryContainer
                )

                else -> Pair(
                    MaterialTheme.colorScheme.tertiaryContainer,
                    MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            IdentityAvatar(
                avatarUrl = member.avatarUrl,
                displayName = member.alias ?: member.profileName,
                backgroundColor = avatarBg,
                contentColor = avatarFg,
                borderColor = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier
                    .size(32.dp)
            )
        }
    }
}

@Composable
fun VisibilityMembers(
    uiState: AwarenessSnapshotUiState
) {
    val visibleTo = uiState.visibleMembers

    val label = when {
        uiState.isEmergency -> "Emergency sharing active"
        uiState.isSharing -> when (visibleTo.size) {
            0 -> "Visible to no one"
            1 -> {
                val name = (visibleTo[0].alias ?: visibleTo[0].profileName).split(" ").first()
                "Visible to $name"
            }

            2 -> {
                val name1 = (visibleTo[0].alias ?: visibleTo[0].profileName).split(" ").first()
                val name2 = (visibleTo[1].alias ?: visibleTo[1].profileName).split(" ").first()
                "Visible to $name1 & $name2"
            }

            else -> "Visible to ${visibleTo.size} people"
        }

        else -> "Visible to no one"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {

        if (uiState.isSharing || uiState.isEmergency) {
            AvatarStack(visibleTo)
        }

        if (visibleTo.isNotEmpty() && (uiState.isSharing || uiState.isEmergency)) {
            Spacer(Modifier.width(8.dp))
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )

    }
}

@Composable
fun MemberAvatar(
    index: Int,
    size: Int = 26,
    modifier: Modifier = Modifier
) {

    val (avatarBg, avatarFg) = when (index % 3) {
        0 -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )

        1 -> Pair(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )

        else -> Pair(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clip(CircleShape)
            .background(avatarBg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "A",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = (size * 0.38f).sp,
                fontWeight = FontWeight.SemiBold,
                color = avatarFg,
            ),
        )
    }
}

@Composable
fun LatestActivitySection(
    totalLovedOnesCount: Int,
    latestActivity: LatestActivity?
) {
    if (totalLovedOnesCount == 0) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "No circle members yet",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "Invite someone to start building awareness together.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    } else if (latestActivity == null) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "No activity yet",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "Recent arrivals and departures will appear here.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IdentityAvatar(
                avatarUrl = latestActivity.actorAvatar,
                displayName = latestActivity.actorName,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Latest in your circle",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                )

                val transitionText = if (latestActivity.isArrival) {
                    "${latestActivity.actorName} arrived at ${latestActivity.placeName}"
                } else {
                    "${latestActivity.actorName} left ${latestActivity.placeName}"
                }

                Text(
                    text = transitionText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }

            Text(
                latestActivity.relativeTime,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AwarenessSnapshotCardPreview() {
    val mockState = AwarenessSnapshotUiState(
        currentPlace = "Home",
        isSharing = true,
        isEmergency = false,
        visibleMembers = listOf(
            CircleMember(
                id = "1",
                profileName = "Nitya",
                alias = "Nitya",
                selected = true,
                receiverEmail = "nitya@geowav.com"
            ),
            CircleMember(
                id = "2",
                profileName = "Diya",
                alias = "Diya",
                selected = true,
                receiverEmail = "diya@geowav.com"
            )
        ),
        latestActivity = LatestActivity(
            actorName = "Nitya",
            actorAvatar = null,
            placeName = "Home",
            isArrival = true,
            relativeTime = "5 min ago"
        ),
        totalLovedOnesCount = 2
    )
    MaterialTheme {
        AwarenessSnapshotCard(uiState = mockState)
    }
}

@Composable
fun InsightPreviewCard(
    insight: Insights? = null,
    scope: PersonalInsightScope? = null,
    heroText: String = "MOST VISITED PLACE",
    ctaText: String = "See Insights",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(20.dp),
            color = GeoWavThemeExtras.colors.periwinkleTintedSurface,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier,
            ) {
                SolidBar(
                    GeoWavThemeExtras.colors.periwinkle
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(GeoWavThemeExtras.colors.periwinkle),
                        )

                        Spacer(Modifier.width(6.dp))

                        Text(
                            text = "INSIGHT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.08.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GeoWavThemeExtras.colors.periwinkle,
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = if (scope == PersonalInsightScope.Month) "This month" else "This week",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.08.sp,
                                fontWeight = FontWeight.W600,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                    }

                    Text(
                        text = heroText,
                        style = MaterialTheme.typography.titleSmall.copy(
                            letterSpacing = 0.08.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )

                    Spacer(Modifier.height(8.dp))

                    val largeDisplay = when (insight) {
                        is Insights.MostVisitedPlaceInsight -> insight.placeName
                        is Insights.AverageVisitDurationInsight -> formatDuration(insight.averageDurationMillis)
                        is Insights.WeeklyAwarenessSummaryInsight -> "${insight.arrivals} Arr / ${insight.departures} Dep"
                        null -> "No visits recorded"
                    }

                    val displaySubtitle = when (insight) {
                        is Insights.MostVisitedPlaceInsight -> {
                            val scopeText =
                                if (insight.scope == PersonalInsightScope.Month) "this month" else "this week"
                            "Visited ${insight.visitCount} ${if (insight.visitCount == 1) "time" else "times"} $scopeText"
                        }

                        is Insights.AverageVisitDurationInsight -> {
                            "Average time spent at ${insight.placeName} · per visit"
                        }

                        is Insights.WeeklyAwarenessSummaryInsight -> {
                            "${insight.placesVisited} ${if (insight.placesVisited == 1) "place" else "places"} visited · Active: ${insight.mostActivePlace ?: "None"}"
                        }

                        null -> "Visit saved places to start building patterns"
                    }

                    Text(
                        text = largeDisplay,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            letterSpacing = 0.08.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = displaySubtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            letterSpacing = 0.08.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeoWavThemeExtras.colors.periwinkle,
                        ),
                    )


//                    Text(
//                        text = "Your most frequent destination",
//                        style = MaterialTheme.typography.bodySmall.copy(
//                            letterSpacing = 0.08.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        ),
//                    )


                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp,
                        color = GeoWavThemeExtras.colors.periwinkle,
                    )

                    FilledTonalButton(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.inverseSurface
                        ),
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = ctaText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                letterSpacing = 0.08.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                            ),
                        )

                        Spacer(Modifier.width(4.dp))

                        Icon(
                            painter = painterResource(R.drawable.caret_right_fill),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.inverseOnSurface,
                        )
                    }
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

@Preview(showBackground = true)
@Composable
fun InsightPreviewCardPreview() {
    GeoWavTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview 1: Most Visited Place Insight
            InsightPreviewCard(
                insight = Insights.MostVisitedPlaceInsight(
                    placeName = "Office",
                    visitCount = 18,
                    scope = PersonalInsightScope.Month
                ),
                scope = PersonalInsightScope.Month,
                heroText = "MOST VISITED PLACE",
                ctaText = "See Insights"
            )

            // Preview 2: Average Visit Duration Insight
            InsightPreviewCard(
                insight = Insights.AverageVisitDurationInsight(
                    placeName = "Gym",
                    averageDurationMillis = 5400000L, // 1h 30m
                    sessionCount = 6,
                    scope = PersonalInsightScope.Week
                ),
                scope = PersonalInsightScope.Week,
                heroText = "AVERAGE TIME SPENT",
                ctaText = "See Insights"
            )

            // Preview 3: Weekly Awareness Summary Insight
            InsightPreviewCard(
                insight = Insights.WeeklyAwarenessSummaryInsight(
                    arrivals = 12,
                    departures = 10,
                    placesVisited = 4,
                    mostActivePlace = "Office",
                    scope = PersonalInsightScope.Week
                ),
                scope = PersonalInsightScope.Week,
                heroText = "WEEKLY AWARENESS SUMMARY",
                ctaText = "See Insights"
            )

            // Preview 4: Null state (No visits recorded)
            InsightPreviewCard(
                insight = null,
                scope = null,
                heroText = "MOST VISITED PLACE",
                ctaText = "See Insights"
            )
        }
    }
}
