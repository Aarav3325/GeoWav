package com.aarav.geowav.presentation.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.data.geofence.ActivityFilter
import com.aarav.geowav.presentation.buildRelativeSubtitle
import com.aarav.geowav.ui.theme.manrope
import com.aarav.geowav.ui.theme.sora

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    activityViewModel: ActivityViewModel
) {


    val uiState by activityViewModel.uiState.collectAsState()

    val lazyState = rememberLazyListState()

//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = "Activity",
//                        fontSize = 24.sp,
//                        fontFamily = manrope,
//                        fontWeight = FontWeight.SemiBold,
//                        color = MaterialTheme.colorScheme.onBackground
//                    )
//                }
//            )
//        }
//    ) {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
        //.padding(it)
    ) {
        Text(
            text = "Activity",
            fontSize = 24.sp,
            fontFamily = manrope,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 54.dp, start = 12.dp, end = 12.dp)
        )

        FilterRow(
            selectedFilter = uiState.currentFilter,
            onFilterSelected = { filter ->
                activityViewModel.onFilterChanged(filter)
            },
            onSetRangeClick = {
                // TODO: open date range picker dialog
                // After user picks dates:
                // activityViewModel.onFilterChanged(ActivityFilter.Between(from, to))
            }
        )

        ActivityContent(uiState)
    }
    //}
}

@Composable
fun ActivityContent(
    uiState: ActivityUiState
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
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
                        fontFamily = sora,
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
                        // reuse your existing AlertItem(alert) from Home screen
                        NewLog2(alert)
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
    selectedFilter: ActivityFilter,
    onFilterSelected: (ActivityFilter) -> Unit,
    onSetRangeClick: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            LogFilterChip("Today", selectedFilter == ActivityFilter.Today) {
                onFilterSelected(ActivityFilter.Today)
            }
        }

        item {
            LogFilterChip("Yesterday", selectedFilter == ActivityFilter.Yesterday) {
                onFilterSelected(ActivityFilter.Yesterday)
            }
        }

        item {
            LogFilterChip("7 days", selectedFilter == ActivityFilter.Last7Days) {
                onFilterSelected(ActivityFilter.Last7Days)
            }
        }

        item {
            LogFilterChip("Set range", selectedFilter is ActivityFilter.Between) {
                onSetRangeClick()
            }
        }
    }
}

@Composable
fun LogFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                label,
                fontSize = 12.sp,
                fontFamily = sora
            )
        },
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .padding(start = 4.dp),
        enabled = true,
        leadingIcon = {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        shape = RoundedCornerShape(10.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = FilterChipDefaults.filterChipElevation(2.dp)
    )
}

@Composable
fun ActivityLog(alert: com.aarav.geowav.data.model.GeoAlert) {
    val isEntry = alert.type == "enter"

    // Semantic styling based on logic
    // val icon = if (isEntry) Icons.Rounded.Login else Icons.Rounded.Logout
    val (bgColor, iconTint) = if (isSystemInDarkTheme()) {
        if (isEntry) 0xFF0F291E to 0xFFA3F2D6 else 0xFF2C1517 to 0xFFFFDAD6
    } else {
        if (isEntry) 0xFFECFDF5 to 0xFF059669 else 0xFFFFF1F2 to 0xFFE11D48
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(bgColor)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(iconTint),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = sora
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = alert.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = manrope
                ),
                maxLines = 1
            )
        }

        Text(
            text = alert.time,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.outline,
                fontFamily = sora
            )
        )
    }
}

@Composable
fun NewLog(alert: com.aarav.geowav.data.model.GeoAlert) {


    val enter = Color(0xFF00513f)

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemInDarkTheme()) {
                if (alert.type.equals("enter")) Color(0xFF00513f) else Color(0xFF723339)
            } else {
                if (alert.type.equals("enter")) Color(0xFFa3f2d6) else Color(0xFFffdadb)
            },
            contentColor =
                if (isSystemInDarkTheme()) {
                    if (alert.type.equals("enter")) Color(0XFFa3f2d6) else Color(0xFFffdadb)
                } else {
                    if (alert.type.equals("enter")) Color(0xFF00513f) else Color(0xFF723339)
                }
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    //.background(Color(0xFFBAFFDF)),
                    .background(MaterialTheme.colorScheme.inverseSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    alert.title,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = sora
                    )
                )
                Text(
                    alert.subtitle,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = sora),
                    maxLines = 2
                )
            }
            Text(
                alert.time,
                fontFamily = sora,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun NewLog2(
    alert: com.aarav.geowav.data.model.GeoAlert,
    modifier: Modifier = Modifier
) {
    val isEnter = alert.type.equals("enter", ignoreCase = true)

    val type = if (alert.type.equals("ENTER", ignoreCase = true)) "enter" else "exit"


    val relativeTime = buildRelativeSubtitle(type, alert.readableTime)

    val containerColor = if (isSystemInDarkTheme()) {
        if (isEnter) Color(0xFF00513f) else Color(0xFF723339)
    } else {
        if (isEnter) Color(0xFFa3f2d6) else Color(0xFFffdadb)
    }

    val contentColor = if (isSystemInDarkTheme()) {
        if (isEnter) Color(0XFFa3f2d6) else Color(0xFFffdadb)
    } else {
        if (isEnter) Color(0xFF00513f) else Color(0xFF723339)
    }

    // Use the Long readableTime (epoch millis) to compute "x mins ago"

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading icon box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.inverseOnSurface
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text area
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title: "Entered X" / "Left X"
                Text(
                    text = alert.title,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = sora
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Subtitle like "Shree Satya Sai Vidyalaya" or "Geofence X • something"
                Text(
                    text = relativeTime,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = sora
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Row for type chip + relative time ("Left 10 mins ago")
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRIGGER TYPE :",           // e.g. "Left 10 mins ago"
                        fontSize = 11.sp,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = sora,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Type chip (ENTER / EXIT)
                    TypeChip(isEnter = isEnter)

                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right side exact time: e.g. "10:45 AM"
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = alert.time, // your preformatted time string
                    fontFamily = sora,
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
    val bg = if (isEnter) {
        MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.25f)
    }

    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontFamily = sora
        )
    }
}
