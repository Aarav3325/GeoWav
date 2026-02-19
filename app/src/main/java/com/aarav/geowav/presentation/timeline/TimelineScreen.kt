package com.aarav.geowav.presentation.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.aarav.geowav.data.model.TimelineItem
import com.aarav.geowav.data.model.toTimelineItem
import com.aarav.geowav.presentation.theme.GeoWavTheme
import com.aarav.geowav.presentation.theme.manrope
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimelineScreen(
    timelineViewModel: TimelineViewModel,
    back: () -> Unit,
    navigateToPreview: (String, String) -> Unit,
    userId: String,
    name: String
) {

    val uiState by timelineViewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        timelineViewModel.getUserSessionHistory(userId)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Timeline of $name",
                        fontFamily = manrope,
                        fontSize = 24.sp,
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
            
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ContainedLoadingIndicator()
                    }
                }

                uiState.sessions.isEmpty() -> {
                    TimelineEmptyState()
                }

                else -> {
                    LazyColumn() {
                        items(uiState.sessions) { session ->
                            TimelineItem(
                                session.toTimelineItem(name),
                                onClick = navigateToPreview,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineItem(
    item: TimelineItem,
    onClick: (String, String) -> Unit,
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = manrope
                    )

                    Text(
                        text = "Location session • $date • $durationText",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = manrope,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }


                Row(
                    modifier = Modifier
                        .padding(0.dp)
                        .clickable(
                            indication = null,
                            interactionSource = null
                        ) {
                            onClick(item.id, item.name)
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
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = item.startAddress ?: "Unknown location",
                            style = MaterialTheme.typography.bodyMedium,
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



