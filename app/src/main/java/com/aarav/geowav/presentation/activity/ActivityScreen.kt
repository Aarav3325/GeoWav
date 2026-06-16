package com.aarav.geowav.presentation.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
        ActivityHeader()

        FilterRow(
            isShowingTimeline = false,
            userPlan = plan,
            selectedFilter = uiState.currentFilter,
            onFilterSelected = activityViewModel::onFilterChanged,
            onUpgradeRequired = { upgradeReason = it },
            onSetRangeClick = activityViewModel::showDatePicker,
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

@Composable
private fun ActivityHeader() {
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
}
