package com.aarav.geowav.presentation.dayreplay

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.data.model.DayReplay
import com.aarav.geowav.data.model.DayReplayStop
import com.aarav.geowav.data.model.DayReplayTimeSection
import com.aarav.geowav.data.model.DayReplayUiItem
import com.aarav.geowav.presentation.theme.manrope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DayReplayTabContent(
    isDarkThemeEnabled: Boolean,
    viewModel: DayReplayViewModel,
    navigateToPlaceDetails: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = maxOf(0, uiState.dates.size - 1),
        pageCount = { uiState.dates.size }
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page in uiState.dates.indices) {
                viewModel.selectDate(uiState.dates[page])
            }
        }
    }

    val selectedDateIndex = uiState.dates.indexOf(uiState.selectedDate)
    val hasPreviousDay = selectedDateIndex > 0
    val hasNextDay = selectedDateIndex < uiState.dates.size - 1

    Column(
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(
                            enabled = hasPreviousDay,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(selectedDateIndex - 1)
                                }
                            }
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "Previous Day",
                        tint = if (hasPreviousDay) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val today = LocalDate.now(ZoneId.of("Asia/Kolkata"))
                    val relativeText = when (uiState.selectedDate) {
                        today -> "Today"
                        today.minusDays(1) -> "Yesterday"
                        else -> uiState.selectedDate.format(DateTimeFormatter.ofPattern("EEEE", Locale.getDefault()))
                    }

                    Text(
                        text = relativeText,
                        fontFamily = manrope,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = uiState.selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())),
                        fontFamily = manrope,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(
                            enabled = hasNextDay,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(selectedDateIndex + 1)
                                }
                            }
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "Next Day",
                        tint = if (hasNextDay) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(180f)
                    )
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ContainedLoadingIndicator()
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    val date = uiState.dates.getOrNull(page)
                    val dayReplay = uiState.dayReplays[date]

                    if (dayReplay == null || dayReplay.stops.isEmpty()) {
                        DayReplayEmptyState()
                    } else {
                        DayReplayContent(
                            isDarkThemeEnabled = isDarkThemeEnabled,
                            replay = dayReplay,
                            expandedStopIds = uiState.expandedStopIds,
                            onToggleStop = { stopId -> viewModel.toggleStopExpanded(stopId) },
                            navigateToPlaceDetails = navigateToPlaceDetails
                        )
                    }
                }
            }
        }
    }

@Composable
fun DayReplayContent(
    isDarkThemeEnabled: Boolean,
    replay: DayReplay,
    expandedStopIds: Set<String>,
    onToggleStop: (String) -> Unit,
    navigateToPlaceDetails: (String) -> Unit
) {
    val uiItems = remember(replay.stops) {
        val items = mutableListOf<DayReplayUiItem>()
        var lastSection: DayReplayTimeSection? = null

        replay.stops.forEach { stop ->
            val section = DayReplayTimeSection.fromTimestamp(stop.arrivedAt)
            if (section != lastSection) {
                items.add(DayReplayUiItem.SectionHeader(section))
                lastSection = section
            }
            items.add(DayReplayUiItem.StopItem(stop))
        }
        items
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(32.dp)
                        .fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .width(3.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, bottom = 28.dp)
                ) {
                    Text(
                        text = replay.heroTitle.uppercase(Locale.getDefault()),
                        fontFamily = manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = replay.heroNarrative,
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        lineHeight = 28.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        items(uiItems) { item ->
            when (item) {
                is DayReplayUiItem.SectionHeader -> {
                    SectionHeaderItem(
                        isDarkThemeEnabled = isDarkThemeEnabled,
                        section = item.section
                    )
                }
                is DayReplayUiItem.StopItem -> {
                    val stop = item.stop
                    val stopId = "${stop.placeId}_${stop.arrivedAt}"
                    val isExpanded = expandedStopIds.contains(stopId)

                    val stopsOnly = replay.stops
                    val isFirst = stopsOnly.firstOrNull() == stop
                    val isLast = stopsOnly.lastOrNull() == stop

                    TimelineStopItem(
                        stop = stop,
                        isStartOrEnd = isFirst || isLast,
                        isLast = isLast,
                        isExpanded = isExpanded,
                        onToggleExpand = { onToggleStop(stopId) },
                        navigateToPlaceDetails = navigateToPlaceDetails
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun SectionHeaderItem(
    isDarkThemeEnabled: Boolean,
    section: DayReplayTimeSection
) {
    val iconRes = when (section) {
        DayReplayTimeSection.MORNING -> R.drawable.morning
        DayReplayTimeSection.AFTERNOON -> R.drawable.afternoon
        DayReplayTimeSection.EVENING -> R.drawable.evening
        DayReplayTimeSection.NIGHT -> R.drawable.night
    }

    val sectionColor = when (section) {
        DayReplayTimeSection.MORNING -> if (isDarkThemeEnabled) Color(0xFFFFB74D) else Color(0xFFE65100)
        DayReplayTimeSection.AFTERNOON -> if (isDarkThemeEnabled) Color(0xFF81D4FA) else Color(0xFF0288D1)
        DayReplayTimeSection.EVENING -> if (isDarkThemeEnabled) Color(0xFFFFAB91) else Color(0xFFD84315)
        DayReplayTimeSection.NIGHT -> if (isDarkThemeEnabled) Color(0xFFB39DDB) else Color(0xFF5E35B1)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, top = 20.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = sectionColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = section.label.uppercase(Locale.getDefault()),
                fontFamily = manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                color = sectionColor
            )
        }
    }
}

@Composable
fun TimelineStopItem(
    stop: DayReplayStop,
    isStartOrEnd: Boolean,
    isLast: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    navigateToPlaceDetails: (String) -> Unit
) {
    val placeIcon = remember(stop.placeName) {
        val name = stop.placeName.lowercase(Locale.getDefault())
        when {
            name.contains("home") || name.contains("house") || name.contains("apartment") || name.contains("residence") -> "🏠"
            name.contains("work") || name.contains("office") || name.contains("job") || name.contains("studio") || name.contains("tech") -> "💼"
            name.contains("starbucks") || name.contains("cafe") || name.contains("coffee") -> "☕"
            name.contains("restaurant") || name.contains("food") || name.contains("eat") || name.contains("diner") || name.contains("pizza") -> "🍴"
            name.contains("gym") || name.contains("fitness") || name.contains("sport") || name.contains("workout") -> "💪"
            name.contains("mall") || name.contains("store") || name.contains("shop") || name.contains("market") -> "🛒"
            name.contains("school") || name.contains("university") || name.contains("college") || name.contains("academy") || name.contains("class") -> "🏫"
            name.contains("hospital") || name.contains("clinic") || name.contains("medical") || name.contains("doctor") -> "🏥"
            name.contains("park") || name.contains("garden") || name.contains("forest") -> "🌳"
            name.contains("airport") || name.contains("station") || name.contains("metro") || name.contains("bus") -> "✈️"
            else -> "📍"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(3.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            )

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            if (!isLast) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(3.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, bottom = 24.dp)
        ) {
            val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrowRotation")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onToggleExpand() }
                    .animateContentSize(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f) else Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = placeIcon,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = stop.placeName,
                                    fontFamily = manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            val timeInterval = if (stop.departedAt != null) {
                                "${formatDayTime(stop.arrivedAt)} – ${formatDayTime(stop.departedAt)}"
                            } else {
                                "Arrived at ${formatDayTime(stop.arrivedAt)}"
                            }

                            Text(
                                text = timeInterval,
                                fontFamily = manrope,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isExpanded) {
                                val durationText = if (stop.stayDurationMillis != null) {
                                    "${formatDuration(stop.stayDurationMillis)}"
                                } else {
                                    "Currently here"
                                }

                                Text(
                                    text = durationText,
                                    fontFamily = manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (stop.departedAt == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier
                                        .background(
                                            color = (if (stop.departedAt == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant).copy(alpha = 0.35f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Icon(
                                painter = painterResource(id = R.drawable.down_arrow),
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .rotate(rotationState)
                            )
                        }
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DetailRow(
                                iconRes = R.drawable.gps,
                                label = "Arrived",
                                value = formatDayTime(stop.arrivedAt)
                            )

                            DetailRow(
                                iconRes = R.drawable.timeline,
                                label = "Left",
                                value = if (stop.departedAt != null) formatDayTime(stop.departedAt) else "Currently here"
                            )

                            DetailRow(
                                iconRes = R.drawable.info,
                                label = "Time Spent",
                                value = if (stop.stayDurationMillis != null) formatDuration(stop.stayDurationMillis) else "Current visit"
                            )

                            DetailRow(
                                iconRes = R.drawable.ruler,
                                label = "Trigger Radius",
                                value = "${stop.radius.toInt()} meters"
                            )

                            stop.address?.let {
                                DetailRow(
                                    iconRes = R.drawable.map_pin,
                                    label = "Location",
                                    value = it
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun borderStroke() = ButtonDefaults.outlinedButtonBorder.copy()

@Composable
fun DetailRow(
    iconRes: Int,
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontFamily = manrope,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = manrope,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 24.dp)
        )
    }
}

@Composable
fun DayReplayEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.timeline),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "No replay yet",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = manrope,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your daily movements will appear here once GeoWav detects visits to your saved places.",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = manrope,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

private fun formatDayTime(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.of("Asia/Kolkata"))
        .format(formatter)
}

private fun formatDuration(durationMillis: Long): String {
    val durationMinutes = durationMillis / 60000
    return when {
        durationMinutes < 1 -> "Less than a minute"
        durationMinutes < 60 -> "Stayed ${durationMinutes}m"
        else -> {
            val hours = durationMinutes / 60
            val minutes = durationMinutes % 60
            if (minutes > 0) "Stayed ${hours}h ${minutes}m" else "Stayed ${hours}h"
        }
    }
}
