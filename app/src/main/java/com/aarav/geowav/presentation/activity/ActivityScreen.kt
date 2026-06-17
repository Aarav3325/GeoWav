package com.aarav.geowav.presentation.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.core.utils.FeatureAccess
import com.aarav.geowav.core.utils.toLocalDateInIndia
import com.aarav.geowav.data.model.UpgradeContext
import com.aarav.geowav.data.model.UpgradeReason
import com.aarav.geowav.presentation.components.CustomBottomSheet
import com.aarav.geowav.presentation.components.MyAlertDialog
import com.aarav.geowav.presentation.components.UpgradeBottomSheetContent
import com.aarav.geowav.presentation.dayreplay.DayReplayTabContent
import com.aarav.geowav.presentation.dayreplay.DayReplayViewModel
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.manrope
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    isDarkThemeEnabled: Boolean,
    activityViewModel: ActivityViewModel,
    dayReplayViewModel: DayReplayViewModel,
    subscriptionViewModel: SubscriptionViewModel,
    navigateToPaywall: () -> Unit,
    navigateToPlaceDetails: (String) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Activity",
                        fontFamily = manrope,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        if (pagerState.currentPage in tabPositions.indices) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    divider = {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        },
                        text = {
                            Text(
                                text = "Circle",
                                fontFamily = manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(1) }
                        },
                        text = {
                            Text(
                                text = "Me",
                                fontFamily = manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> {
                    CircleActivityContent(
                        isDarkThemeEnabled = isDarkThemeEnabled,
                        activityViewModel = activityViewModel,
                        subscriptionViewModel = subscriptionViewModel,
                        navigateToPaywall = navigateToPaywall
                    )
                }
                1 -> {
                    DayReplayTabContent(
                        isDarkThemeEnabled = isDarkThemeEnabled,
                        viewModel = dayReplayViewModel,
                        navigateToPlaceDetails = navigateToPlaceDetails
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleActivityContent(
    isDarkThemeEnabled: Boolean,
    activityViewModel: ActivityViewModel,
    subscriptionViewModel: SubscriptionViewModel,
    navigateToPaywall: () -> Unit
) {
    val uiState by activityViewModel.uiState.collectAsState()
    val plan by subscriptionViewModel.userPlan.collectAsState()

    var upgradeReason by remember { mutableStateOf<UpgradeReason?>(null) }
    var showFutureDateAlert by remember { mutableStateOf(false) }

    val upgradeContext = upgradeReason?.let { reason ->
        FeatureAccess.getUpgradePlan(reason)?.let { plan ->
            UpgradeContext(plan, reason)
        }
    }

    upgradeContext?.let {
        CustomBottomSheet(
            onDismissRequest = { upgradeReason = null }
        ) {
            UpgradeBottomSheetContent(
                context = it,
                onUpgradeClick = {
                    upgradeReason = null
                    navigateToPaywall()
                },
                onDismiss = { upgradeReason = null }
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
        FilterRow(
            isShowingTimeline = false,
            userPlan = plan,
            selectedFilter = uiState.currentFilter,
            onFilterSelected = activityViewModel::onFilterChanged,
            onUpgradeRequired = { upgradeReason = it },
            onSetRangeClick = activityViewModel::showDatePicker,
            modifier = Modifier.padding(top = 12.dp)
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
            DateRangePickerModal(
                onDateRangeSelected = { (from, to) ->
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
                },
                onDismiss = activityViewModel::dismissDatePicker
            )
        }
    }
}
