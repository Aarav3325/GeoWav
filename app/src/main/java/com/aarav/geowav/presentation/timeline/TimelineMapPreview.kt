package com.aarav.geowav.presentation.timeline

// Your custom font
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimelineMapPreview(
    viewModel: TimelineMapPreviewVM,
    back: () -> Unit,
    sessionId: String,
    name: String
) {

    val currentSession by viewModel.currentSession.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.getSessionInfo(sessionId)
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
                        timelineMarkerIcon(MaterialTheme.colorScheme.primary, true)

                    val endIcon =
                        timelineMarkerIcon(MaterialTheme.colorScheme.error, false)


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


                    com.google.maps.android.compose.Polyline(
                        points = listOf(start, end),
                        color = MaterialTheme.colorScheme.inversePrimary,
                        width = 10f
                    )

                    // Animate camera once
                    LaunchedEffect(mapLoaded) {
                        if (mapLoaded) {
                            val bounds = com.google.android.gms.maps.model.LatLngBounds
                                .builder()
                                .include(start)
                                .include(end)
                                .build()

                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngBounds(bounds, 120)
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
                        session = session.toTimelineItem(name),
                        onClose = { showTray = false }
                    )
                }

                AnimatedVisibility(
                    visible = !showTray,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(vertical = 36.dp, horizontal = 24.dp)
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
                        text = "Location session • $durationText",
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

fun createTimelineMarkerBitmap(
    color: Int,
    isStart: Boolean
): Bitmap {

    val size = 96
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
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