package com.aarav.geowav.presentation.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.core.utils.FeatureAccess
import com.aarav.geowav.core.utils.toLocalDateInIndia
import com.aarav.geowav.data.model.TimelineItem
import com.aarav.geowav.data.model.UpgradeContext
import com.aarav.geowav.data.model.UpgradeReason
import com.aarav.geowav.presentation.activity.DateRangePickerModal
import com.aarav.geowav.presentation.activity.FilterRow
import com.aarav.geowav.presentation.components.MyAlertDialog
import com.aarav.geowav.presentation.components.CustomBottomSheet
import com.aarav.geowav.presentation.components.UpgradeBottomSheetContent
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.manrope
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimelineScreen(
    timelineViewModel: TimelineViewModel,
    subscriptionViewModel: SubscriptionViewModel,
    back: () -> Unit,
    navigateToPaywall: () -> Unit,
    navigateToPreview: (String, String, String) -> Unit,
    userId: String,
    name: String
) {

    val uiState by timelineViewModel.uiState.collectAsState()
    val plan by subscriptionViewModel.userPlan.collectAsState()

    var upgradeContext by remember { mutableStateOf<UpgradeContext?>(null) }
    var upgradeReason by remember { mutableStateOf<UpgradeReason?>(null) }

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

    LaunchedEffect(userId) {
        timelineViewModel.observeForFilter(ActivityFilter.Today, userId)
        timelineViewModel.getMySessions(ActivityFilter.Today)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Timeline",
                        fontFamily = manrope,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = back
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = "back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),

            ) {

            var selected by remember {
                mutableStateOf(TimelineOptions.OTHERS_TIMELINE)
            }

            var showFutureDateAlert by remember { mutableStateOf(false) }

            MyAlertDialog(
                shouldShowDialog = showFutureDateAlert,
                onDismissRequest = { showFutureDateAlert = false },
                title = "Invalid Date Range",
                message = "You cannot select dates in the future. Please choose a date range up to today.",
                confirmButtonText = "OK",
                onConfirmClick = { showFutureDateAlert = false }
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
                            timelineViewModel.onFilterChanged(
                                ActivityFilter.Between(
                                    fromDate,
                                    toDate
                                ), userId
                            )
                        }
                    }

                }, onDismiss = {
                    timelineViewModel.dismissDatePicker()
                })
            }


            FilterRow(
                isShowingTimeline = true,
                plan,
                selectedFilter = uiState.currentFilter,
                onFilterSelected = {
                    timelineViewModel.onFilterChanged(it, userId)
                },
                onUpgradeRequired = {
                    upgradeReason = it
                },
                onSetRangeClick = {
                    timelineViewModel.showDatePicker()
                }
            )

            ButtonGroupTimeline(
                selected,
                name
            ) {
                selected = it
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ContainedLoadingIndicator()
                    }
                }
//                uiState.sessions.isEmpty() -> {
//                    TimelineEmptyState()
//                }

                selected == TimelineOptions.OTHERS_TIMELINE -> {
                    if (uiState.sessions.isEmpty()) {
                        TimelineEmptyState()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(uiState.sessions) { session ->
                                TimelineItem(
                                    session,
                                    onClick = navigateToPreview,
                                )
                            }
                        }
                    }
                }

                selected == TimelineOptions.MY_TIMELINE -> {
                    if (uiState.mySessions.isEmpty()) {
                        TimelineEmptyState()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(uiState.mySessions) { session ->
                                TimelineItem(
                                    session,
                                    onClick = navigateToPreview,
                                )
                            }
                        }
                    }
                }

//                else -> {
//                    if (selected == TimelineOptions.OTHERS_TIMELINE) {
////                        LazyColumn() {
////                            items(uiState.sessionsFirebase) { session ->
////                                TimelineItem(
////                                    session,
////                                    onClick = navigateToPreview,
////                                )
////                            }
////                        }
//                    } else {
//                        TimelineEmptyState()
//                    }
//                }
            }
        }
    }
}

@Composable
fun TimelineItem(
    item: TimelineItem,
    onClick: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {

    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    val timeFormatter = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault())
    }

    val date = dateFormatter.format(Date(item.startTime))
    val startTime = timeFormatter.format(Date(item.startTime))
    val endTime = timeFormatter.format(Date(item.endTime))

    val durationMillis = item.endTime - item.startTime
    val durationMinutes = durationMillis / (1000 * 60)

    val durationText = when {
        durationMinutes < 60 -> "${durationMinutes} min"
        else -> {
            val hours = durationMinutes / 60
            val minutes = durationMinutes % 60
            "${hours}h ${minutes}m"
        }
    }


    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = manrope,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Location session • $date • $durationText",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = manrope,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(8.dp))

                Row(
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = null
                        ) {
                            onClick(item.id, item.name, item.userId)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View on map",
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = manrope,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.width(4.dp))

                    Icon(
                        painter = painterResource(R.drawable.right_arrow),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {

                // Timeline indicator
                Column(
                    modifier = Modifier
                        .padding(top = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        Modifier
                            .size(8.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                    )

                    Box(
                        Modifier
                            .width(2.dp)
                            .height(56.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                    )

                    Box(
                        Modifier
                            .size(8.dp)
                            .background(
                                MaterialTheme.colorScheme.secondary,
                                CircleShape
                            )
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {

                    Column {
                        Text(
                            text = "Started at $startTime",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = manrope,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = item.startAddress ?: "Unknown location",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = manrope
                        )
                    }

                    Column {
                        Text(
                            text = "Ended at $endTime",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = manrope,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Text(
                            text = item.endAddress ?: "Unknown location",
                            style = MaterialTheme.typography.bodyMedium,
//                            maxLines = 2,
//                            overflow = TextOverflow.Ellipsis,
                            fontFamily = manrope
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineEmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            painter = painterResource(R.drawable.timeline),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(64.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "No location history yet",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = manrope,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Sessions will appear here once location sharing happens.",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = manrope,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true)
@Composable
fun ButtonGroupTimeline(
    selected: TimelineOptions,
    name: String,
    onClick: (TimelineOptions) -> Unit
) {

    Row(
        Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        TimelineOptions.entries.forEach { timelineOptions ->
            ToggleButton(
                checked = timelineOptions == selected,
                onCheckedChange = {
                    onClick(timelineOptions)
                },
                colors = ToggleButtonDefaults.toggleButtonColors(
                    checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                    checkedContainerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.weight(1f)
            ) {

                val finalName = name.split(" ").first()
                Text(
                    if (timelineOptions == TimelineOptions.MY_TIMELINE) "My Timeline" else "$finalName's Timeline",
                    fontFamily = manrope,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

enum class TimelineOptions(val label: String) {

    OTHERS_TIMELINE("Timeline"),
    MY_TIMELINE("My Timeline")
}



