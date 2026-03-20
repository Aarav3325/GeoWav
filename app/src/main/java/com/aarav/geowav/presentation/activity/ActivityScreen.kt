package com.aarav.geowav.presentation.activity

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.core.utils.FeatureAccess
import com.aarav.geowav.core.utils.toLocalDateInIndia
import com.aarav.geowav.data.model.UpgradeContext
import com.aarav.geowav.data.model.UpgradeReason
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.presentation.components.MyAlertDialog
import com.aarav.geowav.presentation.components.CustomBottomSheet
import com.aarav.geowav.presentation.components.UpgradeBottomSheetContent
import com.aarav.geowav.presentation.home.buildRelativeSubtitle
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.manrope
import java.time.LocalDate

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
        Text(
            text = "Activity",
            fontSize = 20.sp,
            fontFamily = manrope,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 54.dp, start = 16.dp, end = 16.dp)
        )

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
            })

        ActivityContent(isDarkThemeEnabled, uiState)

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


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActivityContent(
    isDarkThemeEnabled: Boolean,
    uiState: ActivityUiState
) {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                ContainedLoadingIndicator()
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

            uiState.alerts.isEmpty() -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(R.drawable.link_break),
                        contentDescription = "break",
                        modifier = Modifier.size(48.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "No activity found",
                        fontFamily = manrope,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Try changing the date range to see previous logs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                // Success state – show list
                LazyColumn(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    items(uiState.alerts) { alert ->
                        NewLog(isDarkThemeEnabled, alert)
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun FilterRow(
    isShowingTimeline: Boolean,
    userPlan: UserPlan,
    selectedFilter: ActivityFilter,
    onFilterSelected: (ActivityFilter) -> Unit,
    onSetRangeClick: () -> Unit,
    onUpgradeRequired: (UpgradeReason) -> Unit
) {
    val isPremiumOrAbove =
        userPlan == UserPlan.PREMIUM || userPlan == UserPlan.PRO

    val isPro = userPlan == UserPlan.PRO

    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 0.dp, top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
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
                "7 days",
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
                "Select Range",
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
            .padding(start = 4.dp)
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
        shape = RoundedCornerShape(10.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = FilterChipDefaults.filterChipElevation(2.dp)
    )
}

@Composable
fun NewLog(
    isDarkThemeEnabled: Boolean,
    alert: com.aarav.geowav.data.model.GeoAlert, modifier: Modifier = Modifier
) {
    val isEnter = alert.type.equals("enter", ignoreCase = true)
    val type = if (alert.type.equals("ENTER", ignoreCase = true)) "enter" else "exit"


    val relativeTime = buildRelativeSubtitle(type, alert.readableTime)

    val containerColor = if (isDarkThemeEnabled) {
        if (isEnter) Color(0xFF00513f) else Color(0xFF723339)
    } else {
        if (isEnter) Color(0xFFa3f2d6) else Color(0xFFffdadb)
    }

    val contentColor = if (isDarkThemeEnabled) {
        if (isEnter) Color(0XFFa3f2d6) else Color(0xFFffdadb)
    } else {
        if (isEnter) Color(0xFF00513f) else Color(0xFF723339)
    }

    val boxColor = Color(0xFFEDEDED)
    val iconColor = Color(0xFF4A4A4A)

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor, contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(boxColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.map_pin),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = alert.title,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold, fontFamily = manrope
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = relativeTime,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = manrope
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trigger:",
                        fontSize = 11.sp,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = manrope, fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    TypeChip(isEnter = isEnter)

                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = alert.time,
                    fontFamily = manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TypeChip(isEnter: Boolean) {
    val label = if (isEnter) "ENTERED" else "LEFT"
    val bg = Color(0xFFEDEDED)

    val textColor = Color(0xFF4A4A4A)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 0.dp), contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontFamily = manrope
        )
    }
}
