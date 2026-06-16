package com.aarav.geowav.presentation.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.data.model.UpgradeReason
import com.aarav.geowav.data.model.UserPlan

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
    val isPremiumOrAbove = userPlan == UserPlan.PREMIUM || userPlan == UserPlan.PRO
    val isPro = userPlan == UserPlan.PRO

    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            LogFilterChip(
                label = "Today",
                selected = selectedFilter == ActivityFilter.Today,
                isLocked = false
            ) {
                onFilterSelected(ActivityFilter.Today)
            }
        }

        item {
            LogFilterChip(
                label = "Yesterday",
                selected = selectedFilter == ActivityFilter.Yesterday,
                isLocked = !isPremiumOrAbove
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
                label = "Past week",
                selected = selectedFilter == ActivityFilter.Last7Days,
                isLocked = !isPro
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
                label = "Custom dates",
                selected = selectedFilter is ActivityFilter.Between,
                isLocked = !isPro
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
            Text(label, fontSize = 12.sp)
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
