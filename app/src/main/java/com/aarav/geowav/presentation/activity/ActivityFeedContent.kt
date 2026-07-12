package com.aarav.geowav.presentation.activity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.data.model.ActivityTransition
import com.aarav.geowav.presentation.components.IdentityAvatar
import com.aarav.geowav.presentation.theme.manrope

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActivityContent(
    isDarkThemeEnabled: Boolean,
    currentUserId: String,
    uiState: ActivityUiState,
    onLoadMore: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                ActivityLoadingState()
            }

            uiState.error != null -> {
                ActivityErrorState(error = uiState.error)
            }

            uiState.activities.isEmpty() -> {
                ActivityEmptyState()
            }

            else -> {
                ActivityFeedList(
                    isDarkThemeEnabled = isDarkThemeEnabled,
                    currentUserId = currentUserId,
                    uiState = uiState,
                    onLoadMore = onLoadMore
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ActivityLoadingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        ContainedLoadingIndicator()
        Text(
            text = "Checking your circle",
            fontFamily = manrope,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Recent arrivals and departures will appear here.",
            fontFamily = manrope,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActivityErrorState(error: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = error,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ActivityEmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.link_break),
            contentDescription = "No activity",
            modifier = Modifier.size(48.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your circle is quiet right now",
            fontFamily = manrope,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "When someone arrives at or leaves a saved place, GeoWav will show it here.",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = manrope,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActivityFeedList(
    isDarkThemeEnabled: Boolean,
    currentUserId: String,
    uiState: ActivityUiState,
    onLoadMore: () -> Unit
) {
    val timelineItems = remember(uiState.activities, uiState.hasMore) {
        transformActivitiesToTimeline(
            activities = uiState.activities,
            hasMoreHistoryInFilter = uiState.hasMore
        )
    }
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false
            lastVisibleIndex >= listState.layoutInfo.totalItemsCount - 5
        }
    }

    LaunchedEffect(shouldLoadMore, uiState.hasMore, uiState.isLoadingMore) {
        if (shouldLoadMore && uiState.hasMore && !uiState.isLoadingMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 83.dp),
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            ActivityFeedSectionHeader(
                filter = uiState.currentFilter,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        items(
            items = timelineItems,
            key = { item -> item.id }
        ) { item ->
            ActivityTimelineRow(
                item = item,
                currentUserId = currentUserId,
                isDarkThemeEnabled = isDarkThemeEnabled,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        item {
            if (uiState.isLoadingMore) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ActivityFeedSectionHeader(
    filter: ActivityFilter,
    modifier: Modifier = Modifier
) {
    val label = when (filter) {
        ActivityFilter.Today -> "Today in your circle"
        ActivityFilter.Yesterday -> "Yesterday in your circle"
        ActivityFilter.Last7Days -> "Past week in your circle"
        is ActivityFilter.Between -> "Selected dates"
    }

    Text(
        text = label,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp),
        fontFamily = manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ActivityTimelineRow(
    item: ActivityTimelineItem,
    currentUserId: String,
    isDarkThemeEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    when (item) {
        is ActivityTimelineItem.Event -> ActivityEventRow(
            item = item,
            currentUserId = currentUserId,
            isDarkThemeEnabled = isDarkThemeEnabled,
            modifier = modifier
        )

        is ActivityTimelineItem.Visit -> ActivityVisitRow(
            item = item,
            currentUserId = currentUserId,
            isDarkThemeEnabled = isDarkThemeEnabled,
            modifier = modifier
        )
    }
}

@Composable
private fun ActivityEventRow(
    item: ActivityTimelineItem.Event,
    currentUserId: String,
    isDarkThemeEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val activity = item.activity
    val story = item.story
    val transition = ActivityTransition.fromRaw(activity.normalizedTransitionType)
    val isArrival = transition == ActivityTransition.ARRIVED
    val actorLabel = if (activity.actorId == currentUserId) "You" else activity.actorName.ifBlank { "Someone" }
    val transitionLabel = if (isArrival) "Arrived" else "Left"
    val relativeTime = remember(activity.timestamp) { activityRelativeTime(activity.timestamp) }
    val exactTime = remember(activity.timestamp) { activityExactTime(activity.timestamp) }
    val accentColor = if (isArrival) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.error
    }
    val containerColor = if (isDarkThemeEnabled) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }
    val storyText = buildAnnotatedString {
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        ) {
            append(actorLabel)
        }
        append(" ")
        append(story.action)
        append(" ")
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        ) {
            append(activity.placeName.ifBlank { "a saved place" })
        }
        story.suffix?.let { suffix ->
            append(" ")
            append(suffix)
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                IdentityAvatar(
                    avatarUrl = activity.actorAvatar,
                    displayName = activity.actorName,
                    backgroundColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.outline,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
                    modifier = Modifier.size(48.dp)
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = storyText,
                    fontFamily = manrope,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = relativeTime,
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = exactTime,
                        fontFamily = manrope,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                color = accentColor.copy(alpha = 0.10f),
                contentColor = accentColor,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = transitionLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontFamily = manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ActivityVisitRow(
    item: ActivityTimelineItem.Visit,
    currentUserId: String,
    isDarkThemeEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val actorLabel = if (item.actorId == currentUserId) "You" else item.actorName.ifBlank { "Someone" }
    val relativeTime = remember(item.endedAt) { activityRelativeTime(item.endedAt) }
    val timeRange = remember(item.startedAt, item.endedAt) {
        activityTimeRange(item.startedAt, item.endedAt)
    }
    val duration = remember(item.durationMillis) {
        activityDuration(item.durationMillis)
    }
    val accentColor = MaterialTheme.colorScheme.tertiary
    val containerColor = if (isDarkThemeEnabled) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }
    val storyText = buildAnnotatedString {
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        ) {
            append(actorLabel)
        }
        append(" visited ")
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        ) {
            append(item.placeName.ifBlank { "a saved place" })
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                IdentityAvatar(
                    avatarUrl = item.actorAvatar,
                    displayName = item.actorName,
                    backgroundColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.outline,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
                    modifier = Modifier.size(48.dp)
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = storyText,
                    fontFamily = manrope,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = timeRange,
                    fontFamily = manrope,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Stayed for $duration",
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = relativeTime,
                        fontFamily = manrope,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                color = accentColor.copy(alpha = 0.10f),
                contentColor = accentColor,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Visit",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontFamily = manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
