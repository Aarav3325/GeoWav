package com.aarav.geowav.presentation.activity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.data.model.ActivityTransition
import com.aarav.geowav.data.model.CircleActivityItem
import com.aarav.geowav.presentation.components.IdentityAvatar
import com.aarav.geowav.presentation.theme.manrope

@Composable
fun CircleSummarySection(
    uiState: ActivityUiState,
    modifier: Modifier = Modifier
) {
    val summary = remember(uiState.activities) {
        buildCircleActivitySummary(uiState.activities)
    }
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.74f)

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Circle Overview",
                fontFamily = manrope,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            when {
                uiState.isLoading -> {
                    Text(
                        text = "Checking recent movement",
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                summary == null -> {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "No recent activity",
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Activity from your circle will appear here as members move between places.",
                            fontFamily = manrope,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    CirclePulseContent(
                        summary = summary,
                        periodLabel = activePeriodLabel(uiState.currentFilter)
                    )
                }
            }
        }
    }
}

@Composable
private fun CirclePulseContent(
    summary: CircleActivitySummary,
    periodLabel: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text(
                text = "${summary.activeMemberCount} ${memberLabel(summary.activeMemberCount)} active $periodLabel",
                fontFamily = manrope,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                lineHeight = 23.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${summary.activityUpdateCount} ${activityUpdateLabel(summary.activityUpdateCount)} $periodLabel",
                fontFamily = manrope,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
            )
        }

        summary.latestActivity?.let { latestActivity ->
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Latest activity",
                    fontFamily = manrope,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    IdentityAvatar(
                        avatarUrl = latestActivity.actorAvatar,
                        displayName = latestActivity.actorName,
                        backgroundColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.outline,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
                        modifier = Modifier.size(44.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = buildSummaryLatestActivityText(latestActivity),
                            fontFamily = manrope,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = activityRelativeTime(latestActivity.timestamp),
                            fontFamily = manrope,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f)
                        )
                    }
                }
            }
        }
    }
}

private data class CircleActivitySummary(
    val activeMemberCount: Int,
    val activityUpdateCount: Int,
    val latestActivity: CircleActivityItem?
)

private fun buildCircleActivitySummary(
    activities: List<CircleActivityItem>
): CircleActivitySummary? {
    val validActivities = activities.filter { item ->
        item.actorId.isNotBlank() &&
                item.placeName.isNotBlank() &&
                ActivityTransition.fromRaw(item.normalizedTransitionType) != null
    }

    if (validActivities.isEmpty()) return null

    return CircleActivitySummary(
        activeMemberCount = validActivities.distinctBy { item -> item.actorId }.size,
        activityUpdateCount = validActivities.size,
        latestActivity = validActivities.maxByOrNull { item -> item.timestamp }
    )
}

private fun memberLabel(count: Int): String = if (count == 1) "member" else "members"

private fun activityUpdateLabel(count: Int): String =
    if (count == 1) "activity" else "activities"

private fun activePeriodLabel(filter: ActivityFilter): String {
    return when (filter) {
        ActivityFilter.Today -> "today"
        ActivityFilter.Yesterday -> "yesterday"
        ActivityFilter.Last7Days -> "this week"
        is ActivityFilter.Between -> "in this period"
    }
}

private fun buildSummaryLatestActivityText(activity: CircleActivityItem): String {
    val actorLabel = activity.actorName.ifBlank { "Someone" }
    val placeLabel = activity.placeName.ifBlank { "a saved place" }
    val action = when (ActivityTransition.fromRaw(activity.normalizedTransitionType)) {
        ActivityTransition.ARRIVED -> "arrived at"
        ActivityTransition.LEFT -> "left"
        null -> "updated"
    }

    return "$actorLabel $action $placeLabel"
}
