package com.aarav.geowav.presentation.timeline

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.data.model.TimelineItem
import com.aarav.geowav.data.model.toTimelineItem
import com.aarav.geowav.presentation.theme.GeoWavTheme
import com.aarav.geowav.presentation.theme.manrope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.createBitmap
import com.aarav.geowav.data.model.toLatLng
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Polyline

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimelineMapPreview(
    viewModel: TimelineMapPreviewVM,
    back: () -> Unit,
    sessionId: String,
    name: String,
    userId: String
) {

    val currentSession by viewModel.currentSession.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.getSessionInfo(sessionId, userId)
    }

    val cameraPositionState = rememberCameraPositionState()
    var mapLoaded by remember { mutableStateOf(false) }
    var showTray by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Session Preview",
                        fontFamily = manrope,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
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
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            if(!mapLoaded) {
                ContainedLoadingIndicator(
                    Modifier.align(Alignment.Center)
                )
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    compassEnabled = true,
                    mapToolbarEnabled = false
                ),
                onMapLoaded = { mapLoaded = true }
            ) {

                currentSession?.let { session ->

                    val start = LatLng(session.startLat, session.startLng)
                    val end = LatLng(session.endLat, session.endLng)

                    val startIcon =
                        timelineMarkerIcon(androidx.compose.ui.graphics.Color(0xFF515B92), true)

                    val endIcon =
                        timelineMarkerIcon(androidx.compose.ui.graphics.Color(0xFF904A44), false)


                    Marker(
                        state = MarkerState(position = start),
                        icon = startIcon,
                        title = "Start: ${currentSession?.startAddress}",
                        anchor = Offset(0.5f, 0.5f)
                    )

                    Marker(
                        state = MarkerState(position = end),
                        icon = endIcon,
                        title = "End: ${currentSession?.endAddress}",
                        anchor = Offset(0.5f, 0.5f)
                    )

                    val userPaths = session.userPath.map {
                        it.toLatLng()
                    }

                    Log.i("SESSION", session.userPath.toString())

                    com.google.maps.android.compose.Polyline(
                        points = userPaths,
                        color = androidx.compose.ui.graphics.Color(0xFF0A6780),
                        width = 10f
                    )

                    // Replay Stay Point Markers
                    session.stayPoints.forEach { stay ->
                        val stayPos = LatLng(stay.lat, stay.lng)
                        val mins = stay.durationMillis / 60_000
                        val durationText = if (mins < 60) "Stayed $mins min"
                                           else "${mins / 60}h ${mins % 60}m"

                        val timeFormatter = remember {
                            java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                        }
                        val startStr = timeFormatter.format(java.util.Date(stay.startedAt))
                        val endStr = timeFormatter.format(java.util.Date(stay.endedAt))

                        Marker(
                            state = MarkerState(position = stayPos),
                            icon = replayStayPointMarkerIcon(),
                            title = durationText,
                            snippet = "$startStr – $endStr",
                            anchor = Offset(0.5f, 0.5f)
                        )
                    }

                    // Animate camera once
                    LaunchedEffect(mapLoaded) {
                        if (mapLoaded) {
                            val boundsBuilder = com.google.android.gms.maps.model.LatLngBounds
                                .builder()
                                .include(start)
                                .include(end)

                            // Include stay points in camera bounds
                            session.stayPoints.forEach { stay ->
                                boundsBuilder.include(LatLng(stay.lat, stay.lng))
                            }

                            val bounds = boundsBuilder.build()

                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngBounds(bounds, 230)
                            )
                        }
                    }
                }
            }

            // Tray
            currentSession?.let { session ->

                AnimatedVisibility(
                    visible = showTray,
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    SessionPreviewTray(
                        session = session,
                        onClose = { showTray = false },
                        Modifier.padding(bottom = 32.dp)
                    )
                }

                AnimatedVisibility(
                    visible = !showTray,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(vertical = 32.dp, horizontal = 16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(62.dp)
                            .clickable { showTray = true }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.info),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun SessionPreviewTray(
    session: TimelineItem,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {

    val timeFormatter = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault())
    }

    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    val date = dateFormatter.format(Date(session.startTime))

    val startTime = timeFormatter.format(Date(session.startTime))
    val endTime = timeFormatter.format(Date(session.endTime))

    val durationMinutes =
        (session.endTime - session.startTime) / (1000 * 60)

    val durationText = if (durationMinutes < 60) {
        "$durationMinutes min"
    } else {
        val hours = durationMinutes / 60
        val mins = durationMinutes % 60
        "${hours}h ${mins}m"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright.copy(0.95f)
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceBright.copy(0.95f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text(
                        text = session.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = manrope
                    )

                    Text(
                        text = "Location session • $date • $durationText",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = manrope,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(onClick = onClose) {
                    Text(
                        "Close",
                        fontFamily = manrope
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Started at $startTime",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = session.startAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = manrope,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Ended at $endTime",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.secondary
                )

                Text(
                    text = session.endAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = manrope,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis
                )
            }

            // Stay Points summary
            if (session.stayPoints.isNotEmpty()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline
                )

                val stayCount = session.stayPoints.size
                val totalStayMins = session.stayPoints.sumOf { it.durationMillis } / 60_000
                val totalStayText = if (totalStayMins < 60) "$totalStayMins min"
                                    else "${totalStayMins / 60}h ${totalStayMins % 60}m"

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$stayCount Stay Point${if (stayCount > 1) "s" else ""}",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = manrope,
                        color = androidx.compose.ui.graphics.Color(0xFFFF9800)
                    )

                    Text(
                        text = "Total time stayed: $totalStayText",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = manrope,
                    )
                }
            }
        }
    }
}

fun createTimelineMarkerBitmap(
    color: Int,
    isStart: Boolean
): Bitmap {

    val size = 96
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Outer soft glow
    paint.color = color
    paint.alpha = 60
    canvas.drawCircle(size / 2f, size / 2f, size / 2.2f, paint)

    // Main circle
    paint.alpha = 255
    paint.color = color
    canvas.drawCircle(size / 2f, size / 2f, size / 3f, paint)

    // White border
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 6f
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 3f, paint)

    return bitmap
}

fun timelineMarkerIcon(
    color: androidx.compose.ui.graphics.Color,
    isStart: Boolean
): BitmapDescriptor {
    return BitmapDescriptorFactory.fromBitmap(
        createTimelineMarkerBitmap(
            color = color.toArgb(),
            isStart = isStart
        )
    )
}

/**
 * Orange marker icon for stay points shown in session replay.
 */
fun replayStayPointMarkerIcon(): BitmapDescriptor {
    val size = 72
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Outer glow
    paint.color = android.graphics.Color.parseColor("#FF9800")
    paint.alpha = 60
    canvas.drawCircle(size / 2f, size / 2f, size / 2.2f, paint)

    // Main circle
    paint.alpha = 255
    paint.color = android.graphics.Color.parseColor("#FF9800")
    canvas.drawCircle(size / 2f, size / 2f, size / 3f, paint)

    // White border
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 5f
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 3f, paint)

    // Inner dot
    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 8f, paint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}


@Preview(showBackground = true)
@Composable
fun SessionPreviewTrayPreview() {

    val dummyItem = TimelineItem(
        id = "1",
        userId = "user_123",
        name = "Aarav",
        startLat = 23.0395,
        startLng = 72.5660,
        endLat = 23.0720,
        endLng = 72.5280,
        startTime = System.currentTimeMillis() - (25 * 60 * 1000),
        endTime = System.currentTimeMillis(),
        startAddress = "Vastrapur, Ahmedabad",
        endAddress = "Gota, Ahmedabad"
    )

    GeoWavTheme {
        SessionPreviewTray(
            session = dummyItem,
            onClose = {},
            Modifier
        )
    }
}