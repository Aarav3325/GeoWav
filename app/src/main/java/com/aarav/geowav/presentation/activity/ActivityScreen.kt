package com.aarav.geowav.presentation.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.core.utils.FeatureAccess
import com.aarav.geowav.core.utils.toLocalDateInIndia
import com.aarav.geowav.data.model.ActivityTransition
import com.aarav.geowav.data.model.CircleActivityItem
import com.aarav.geowav.data.model.UpgradeContext
import com.aarav.geowav.data.model.UpgradeReason
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.presentation.components.CustomBottomSheet
import com.aarav.geowav.presentation.components.IdentityAvatar
import com.aarav.geowav.presentation.components.MyAlertDialog
import com.aarav.geowav.presentation.components.UpgradeBottomSheetContent
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.manrope
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    isDarkThemeEnabled: Boolean,
    activityViewModel: ActivityViewModel,
    subscriptionViewModel: SubscriptionViewModel,
    navigateToPaywall: () -> Unit
) {


    val uiState by activityViewModel.uiState.collectAsState()
    val plan by subscriptionViewModel.userPlan.collectAsState()

    var upgradeContext by remember { mutableStateOf<UpgradeContext?>(null) }
    var upgradeReason by remember { mutableStateOf<UpgradeReason?>(null) }

    var showFutureDateAlert by remember { mutableStateOf(false) }

    upgradeContext = upgradeReason?.let { reason ->
        val plan = FeatureAccess.getUpgradePlan(reason)
        plan?.let { UpgradeContext(it, reason) }
    }


    upgradeContext?.let {
        CustomBottomSheet(
            onDismissRequest = {
                upgradeContext = null
                upgradeReason = null
            }
        ) {
            UpgradeBottomSheetContent(
                context = it,
                onUpgradeClick = {
                    upgradeContext = null
                    upgradeReason = null
                    navigateToPaywall()
                },
                onDismiss = {
                    upgradeContext = null
                    upgradeReason = null
                }
            )
        }
    }

    MyAlertDialog(
        shouldShowDialog = showFutureDateAlert,
        onDismissRequest = { showFutureDateAlert = false },
        title = "Invalid Date Range",
        message = "You cannot select dates in the future. Please choose a date range up to today.",
        confirmButtonText = "OK",
        onConfirmClick = { showFutureDateAlert = false }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)

    ) {
        Column(
            modifier = Modifier.padding(top = 56.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
        ) {
            Text(
                text = "Circle activity",
                fontSize = 28.sp,
                fontFamily = manrope,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Arrivals and departures from the places your circle cares about.",
                fontSize = 13.sp,
                fontFamily = manrope,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        FilterRow(
            isShowingTimeline = false,
            plan,
            selectedFilter = uiState.currentFilter,
            onFilterSelected = { filter ->
                activityViewModel.onFilterChanged(filter)
            },
            onUpgradeRequired = {
                upgradeReason = it
            },
            onSetRangeClick = {
                activityViewModel.showDatePicker()
            },
            modifier = Modifier.padding(top = 0.dp)
            )

        CircleSummarySection(
            uiState = uiState,
            modifier = Modifier.padding(start = 12.dp, top = 10.dp, end = 12.dp)
        )

        ActivityContent(
            isDarkThemeEnabled = isDarkThemeEnabled,
            currentUserId = activityViewModel.viewerId,
            uiState = uiState,
            onLoadMore = activityViewModel::loadMore
        )

        if (uiState.showDatePicker) {
            DateRangePickerModal(onDateRangeSelected = { (from, to) ->
                if (from != null && to != null) {
                    val fromDate = from.toLocalDateInIndia()
                    val toDate = to.toLocalDateInIndia()
                    val today = LocalDate.now()

                    if (fromDate.isAfter(today) || toDate.isAfter(today)) {
                        showFutureDateAlert = true
                    } else {
                        activityViewModel.onFilterChanged(ActivityFilter.Between(fromDate, toDate))
                    }
                }

            }, onDismiss = {
                activityViewModel.dismissDatePicker()
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit, onDismiss: () -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis < System.currentTimeMillis()
            }
        }
    )

    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(
            onClick = {
                onDateRangeSelected(
                    Pair(
                        dateRangePickerState.selectedStartDateMillis,
                        dateRangePickerState.selectedEndDateMillis
                    )
                )
                onDismiss()
            }) {
            Text("OK")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Cancel")
        }
    }) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    text = "Select date range"
                )
            },
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp)
        )
    }
}


@Composable
private fun CircleSummarySection(
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
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(
                            text = "${summary.activeMemberCount} ${memberLabel(summary.activeMemberCount)} active",
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "${summary.activityUpdateCount} ${activityUpdateLabel(summary.activityUpdateCount)}",
                            fontFamily = manrope,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        summary.latestActivity?.let { latestActivity ->
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Latest activity",
                                    fontFamily = manrope,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = buildSummaryLatestActivityText(latestActivity),
                                    fontFamily = manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
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
    if (count == 1) "activity update" else "activity updates"

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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActivityContent(
    isDarkThemeEnabled: Boolean,
    currentUserId: String,
    uiState: ActivityUiState,
    onLoadMore: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
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

            uiState.error != null -> {
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
                        text = uiState.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            uiState.activities.isEmpty() -> {
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
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

                    items(uiState.activities) { activity ->
                        ActivityFeedItem(
                            activity = activity,
                            story = remember(activity, uiState.activities, uiState.hasMore) {
                                deriveActivityStory(
                                    activity = activity,
                                    activities = uiState.activities,
                                    hasMoreHistoryInFilter = uiState.hasMore
                                )
                            },
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
private fun ActivityFeedItem(
    activity: CircleActivityItem,
    story: ActivityStory,
    currentUserId: String,
    isDarkThemeEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val isArrival = activity.normalizedTransitionType == ActivityTransition.ARRIVED.name
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
                .padding(horizontal = 12.dp, vertical = 12.dp),
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

private data class ActivityStory(
    val action: String,
    val suffix: String? = null,
    val reason: StoryReason = StoryReason.Original
)

private enum class StoryReason {
    Original,
    BackAfterLeavingSamePlace,
    FirstActivityOfDay,
    PreviousArrivalAtPlace
}

private fun deriveActivityStory(
    activity: CircleActivityItem,
    activities: List<CircleActivityItem>,
    hasMoreHistoryInFilter: Boolean
): ActivityStory {
    val transition = ActivityTransition.fromRaw(activity.normalizedTransitionType)
        ?: return ActivityStory(action = "was at")

    if (transition == ActivityTransition.LEFT) {
        return ActivityStory(action = "left")
    }

    val olderActorActivities = activities
        .filter { item ->
            item.actorId == activity.actorId &&
                    item.timestamp < activity.timestamp
        }
        .sortedByDescending { it.timestamp }

    val previousActorActivity = olderActorActivities.firstOrNull()
    if (
        previousActorActivity?.placeName == activity.placeName &&
        ActivityTransition.fromRaw(previousActorActivity.normalizedTransitionType) == ActivityTransition.LEFT
    ) {
        return ActivityStory(
            action = "is back at",
            reason = StoryReason.BackAfterLeavingSamePlace
        )
    }

    if (!hasMoreHistoryInFilter && olderActorActivities.none { item ->
            item.timestamp.toLocalDateInIndia() == activity.timestamp.toLocalDateInIndia()
        }
    ) {
        return ActivityStory(
            action = "started the day at",
            reason = StoryReason.FirstActivityOfDay
        )
    }

    val hasOlderArrivalAtPlace = olderActorActivities.any { item ->
        item.placeName == activity.placeName &&
                ActivityTransition.fromRaw(item.normalizedTransitionType) == ActivityTransition.ARRIVED
    }
    if (hasOlderArrivalAtPlace) {
        return ActivityStory(
            action = "returned to",
            reason = StoryReason.PreviousArrivalAtPlace
        )
    }

    return ActivityStory(action = "arrived at")
}

private fun activityRelativeTime(timestamp: Long): String {
    val diffMillis = (System.currentTimeMillis() - timestamp).coerceAtLeast(0L)
    val minutes = diffMillis / 60_000L
    val hours = minutes / 60L
    val days = hours / 24L

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days == 1L -> "Yesterday"
        days < 7 -> "${days}d ago"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun activityExactTime(timestamp: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
}

@Composable
fun FilterRow(
    isShowingTimeline: Boolean,
    userPlan: UserPlan,
    selectedFilter: ActivityFilter,
    onFilterSelected: (ActivityFilter) -> Unit,
    onSetRangeClick: () -> Unit,
    onUpgradeRequired: (UpgradeReason) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPremiumOrAbove =
        userPlan == UserPlan.PREMIUM || userPlan == UserPlan.PRO

    val isPro = userPlan == UserPlan.PRO

    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            LogFilterChip(
                "Today",
                selectedFilter == ActivityFilter.Today,
                false
            ) {
                onFilterSelected(ActivityFilter.Today)
            }
        }

        item {
            LogFilterChip(
                "Yesterday",
                selectedFilter == ActivityFilter.Yesterday,
                !isPremiumOrAbove
            ) {
                if (isPremiumOrAbove) {
                    onFilterSelected(ActivityFilter.Yesterday)
                } else {
                    onUpgradeRequired(
                        if (isShowingTimeline) UpgradeReason.TimelineYesterday else UpgradeReason.ActivityYesterday
                    )
                }
            }
        }

        item {
            LogFilterChip(
                "Past week",
                selectedFilter == ActivityFilter.Last7Days,
                !isPro
            ) {
                if (isPro) {
                    onFilterSelected(ActivityFilter.Last7Days)
                } else {
                    onUpgradeRequired(
                        if (isShowingTimeline) UpgradeReason.FullTimelineAccess else UpgradeReason.FullActivityHistoryAccess
                    )
                }
            }
        }

        item {
            LogFilterChip(
                "Custom dates",
                selectedFilter is ActivityFilter.Between,
                !isPro
            ) {
                if (isPro) {
                    onSetRangeClick()
                } else {
                    onUpgradeRequired(
                        if (isShowingTimeline) UpgradeReason.FullTimelineAccess else UpgradeReason.FullActivityHistoryAccess
                    )
                }
            }
        }
    }
}

@Composable
fun LogFilterChip(
    label: String,
    selected: Boolean,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                label, fontSize = 12.sp,
            )
        },
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .padding(start = 0.dp)
            .alpha(if (isLocked) 0.7f else 1f),
        leadingIcon = {
            if (selected) {
                Icon(
                    painter = painterResource(R.drawable.check),
                    contentDescription = "",
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            } else if (isLocked) {
                Icon(
                    painter = painterResource(R.drawable.lock),
                    contentDescription = "",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        },
        shape = RoundedCornerShape(100.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = Color.Transparent,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = Color.Transparent,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = null
    )
}
