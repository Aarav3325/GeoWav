package com.aarav.geowav.presentation.timeline

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.createBitmap
import com.aarav.geowav.R
import com.aarav.geowav.data.model.StayPoint
import com.aarav.geowav.data.model.TimelineItem
import com.aarav.geowav.data.model.toLatLng
import com.aarav.geowav.presentation.theme.GeoWavTheme
import com.aarav.geowav.presentation.theme.manrope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimelineMapPreview(
    viewModel: TimelineMapPreviewVM,
    back: () -> Unit,
    sessionId: String,
    userId: String
) {

    val currentSession by viewModel.currentSession.collectAsState()
    val snappedPath by viewModel.snappedPath.collectAsState()

    // Current playback state
    var isPlaying by remember {
        mutableStateOf(false)
    }

    // Used to check if camera is moving
    var followUser by remember { mutableStateOf(true) }

    // Index of current playback point
    var playbackIndex by remember {
        mutableIntStateOf(0)
    }

    // Animated path for playback polyline rendering
    val animatedPath = remember {
        androidx.compose.runtime.mutableStateListOf<LatLng>()
    }

    // Determines playback speed
    var speed by remember {
        mutableStateOf(1f)
    }

    // Stay points for current session
    val revealedStayPoints = remember {
        mutableStateListOf<StayPoint>()
    }

    // Marker state for moving marker
    val movingMarkerState = remember { MarkerState() }

    // Determine last marker position during playback in order to
    // play and pause playback precisely
    var lastPosition by remember { mutableStateOf<LatLng?>(null) }

    // Used to change map type
    var mapType by remember { mutableStateOf(MapType.NORMAL) }

    // Check if map is loaded
    var mapLoaded by remember { mutableStateOf(false) }

    val startLatLng = remember(currentSession?.id) {
        currentSession?.let { LatLng(it.startLat, it.startLng) }
    }

    val endLatLng = remember(currentSession?.id) {
        currentSession?.let { LatLng(it.endLat, it.endLng) }
    }

    // Start and end marker states for current session
    val startMarkerState = remember { MarkerState() }
    val endMarkerState = remember { MarkerState() }

    // Start and end marker icons
    val startIcon = remember(mapLoaded) {
        if (mapLoaded) timelineMarkerIcon(Color(0xFF515B92), true) else null
    }

    val endIcon = remember(mapLoaded) {
        if (mapLoaded) timelineMarkerIcon(Color(0xFF904A44), false) else null
    }

    // Moving marker icon
    val movingIcon = remember(mapLoaded) {
        if (mapLoaded) movingPlaybackMarkerIcon() else null
    }

    // Stay point marker icon
    val stayIcon = remember(mapLoaded) {
        if (mapLoaded) replayStayPointMarkerIcon() else null
    }

    LaunchedEffect(sessionId) {
        viewModel.getSessionInfo(sessionId, userId)
    }


    val cameraPositionState = rememberCameraPositionState()

    var showTray by remember { mutableStateOf(true) }

    var show by remember { mutableStateOf(false) }

    LaunchedEffect(show) {
        delay(1500)
        show = false
    }

    // Check if camera is moving or not
    LaunchedEffect(cameraPositionState) {
        snapshotFlow {
            cameraPositionState.isMoving to
                    cameraPositionState.cameraMoveStartedReason
        }.collect { (isMoving, reason) ->
            if (isMoving && reason == CameraMoveStartedReason.GESTURE) {
                followUser = false
            }
        }
    }

    Log.i("MAP", mapType.toString())

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

        LaunchedEffect(currentSession?.id) {
            currentSession?.let {
                startMarkerState.position = LatLng(it.startLat, it.startLng)
                endMarkerState.position = LatLng(it.endLat, it.endLng)
            }
        }

        val userPaths = remember(currentSession) {
            currentSession?.userPath?.map { it.toLatLng() }
        }

        val path = userPaths?.joinToString("|") {
            "${it.latitude},${it.longitude}"
        }

        val context = LocalContext.current

        LaunchedEffect(path) {
            if (!path.isNullOrEmpty()) {
                viewModel.getSnappedPath(
                    path,
                    true,
                    context.getString(R.string.maps_api)
                )
            }
        }

        val finalSnappedPath = remember(snappedPath) {
            snappedPath.map {
                LatLng(it.location.latitude, it.location.longitude)
            }
        }


        Log.i(
            "SNAP",
            "path: " + path.toString()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {


            if (!mapLoaded) {
                ContainedLoadingIndicator(
                    Modifier.align(Alignment.Center)
                )
            }

            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = mapType
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

                    startIcon?.let {
                        Marker(
                            state = startMarkerState,
                            icon = startIcon,
                            title = "Start: ${session.startAddress}",
                            anchor = Offset(0.5f, 0.5f)
                        )
                    }

                    endIcon?.let {
                        Marker(
                            state = endMarkerState,
                            icon = endIcon,
                            title = "End: ${session.endAddress}",
                            anchor = Offset(0.5f, 0.5f)
                        )
                    }

                    Log.i("SESSION", session.userPath.toString())

                    /* Moving marker
                       Only show when playback is active also in order to handle pause and resume case
                       we use lastPosition check so it does not disappear when user pauses playback
                     */
                    if (playbackIndex != 0 || lastPosition != null) {
                        Marker(
                            state = movingMarkerState,
                            icon = movingIcon,
                            anchor = Offset(0.5f, 0.5f),
                            title = "Playback"
                        )
                    }


                    // Show user path polyline during playback (animated path)
                    if (animatedPath.isNotEmpty()) {
                        com.google.maps.android.compose.Polyline(
                            points = animatedPath.toList(),
                            color = androidx.compose.ui.graphics.Color(0xFF0A6780),
                            width = 10f
                        )
                    } else {
                        // Show user path polyline when playback is not active (show whole path)
                        if (userPaths != null) {
                            com.google.maps.android.compose.Polyline(
                                points = finalSnappedPath,
                                color = androidx.compose.ui.graphics.Color(0xFF0A6780),
                                width = 10f
                            )
                        }
                    }


                    // Replay Stay Point Markers
                    if (revealedStayPoints.isNotEmpty()) {
                        revealedStayPoints.forEach { stay ->

                            val stayPos = LatLng(stay.lat, stay.lng)

                            val mins = stay.durationMillis / 60_000
                            val durationText =
                                if (mins < 60) "Stayed $mins min"
                                else "${mins / 60}h ${mins % 60}m"

                            val timeFormatter = remember {
                                SimpleDateFormat("hh:mm a", Locale.getDefault())
                            }

                            val startStr = timeFormatter.format(Date(stay.startedAt))
                            val endStr = timeFormatter.format(Date(stay.endedAt))

                            Marker(
                                state = MarkerState(position = stayPos),
                                icon = stayIcon,
                                title = durationText,
                                snippet = "$startStr – $endStr",
                                anchor = Offset(0.5f, 0.5f)
                            )
                        }
                    } else {
                        session.stayPoints.forEach { stay ->

                            val stayPos = LatLng(stay.lat, stay.lng)

                            val mins = stay.durationMillis / 60_000
                            val durationText =
                                if (mins < 60) "Stayed $mins min"
                                else "${mins / 60}h ${mins % 60}m"

                            val timeFormatter = remember {
                                SimpleDateFormat("hh:mm a", Locale.getDefault())
                            }

                            val startStr = timeFormatter.format(Date(stay.startedAt))
                            val endStr = timeFormatter.format(Date(stay.endedAt))

                            Marker(
                                state = MarkerState(position = stayPos),
                                icon = stayIcon,
                                title = durationText,
                                snippet = "$startStr – $endStr",
                                anchor = Offset(0.5f, 0.5f)
                            )
                        }
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

            val mapMode = when (mapType) {
                MapType.NORMAL -> "Normal"
                MapType.SATELLITE -> "Satellite"
                MapType.TERRAIN -> "Terrain"
                MapType.HYBRID -> "Hybrid"
                else -> "Map"
            }

            AnimatedVisibility(
                show,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Text(
                        text = "Switched to $mapMode mode",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }


            /*
            User path playback logic
             */

            LaunchedEffect(isPlaying) {

                if (!isPlaying) return@LaunchedEffect

                if (userPaths != null) {

                    // Iterate through path segments
                    // playbackIndex stores current segment
                    for (i in playbackIndex until finalSnappedPath.size - 1) {

                        // Ensure the loop exits immediately if playback is paused
                        if (!isPlaying) return@LaunchedEffect

                        /*  Start is either last animated coordinate or segment start point
                            This prevent the marker from jumping back to segment start point when playback resumes
                         */
                        val start = lastPosition ?: userPaths[i]
                        val end = finalSnappedPath[i + 1] // end point

                        val distance = com.google.maps.android.SphericalUtil
                            .computeDistanceBetween(start, end)

                        val baseSpeed = 35.0 // meters per second
                        val duration = (distance / (baseSpeed * speed) * 1000).toLong()

                        val steps = (duration / 16).toInt().coerceAtLeast(1)
                        /*
                         Instead of jumping directly between 2 points,
                         we interpolate 20 intermediate points between them
                         in order to create a smooth playback experience
                         */

                        // Calculate intermediate positions between start and end points
                        for (step in 0..steps) {

                            if (!isPlaying) return@LaunchedEffect

                            val fraction = step / steps.toFloat()

//                                val interpolated = SphericalUtil.interpolate(start, end, fraction.toDouble())

                            // Compute the interpolated LatLng using the provided function
                            val interpolated = SphericalUtil.interpolate(start, end, fraction.toDouble())

                            // update user marker position
                            movingMarkerState.position = interpolated

                            // draw path gradually
                            animatedPath.add(interpolated)

                            /* Update last position for pause and resume
                               so that camera continues from last interpolated coordinate
                             */
                            lastPosition = interpolated

                            /*
                                If follow is enabled then the map camera moves with playback marker
                             */
                            if (step % 2 == 0 && followUser) {
                                cameraPositionState.move(
                                    CameraUpdateFactory.newLatLng(interpolated)
                                )
                            }

                            delay((duration/steps))
                        }

                        // update the current segment
                        playbackIndex = i + 1

                        // Detect stay points
                        val currentPoint = finalSnappedPath[playbackIndex] // actual reached point

                        currentSession?.stayPoints?.forEach { stay ->

                            // Prevent duplicate stay point markers
                            if (revealedStayPoints.contains(stay)) return@forEach

                            val stayPos = LatLng(stay.lat, stay.lng)

                            // Compute distance between current point and stay point
                            val distance =
                                com.google.maps.android.SphericalUtil
                                    .computeDistanceBetween(currentPoint, stayPos)

                            // Add stay point if distance is less than 30 meters
                            if (distance < 30) {
                                revealedStayPoints.add(stay)
                            }
                        }
                    }
                }

                isPlaying = false
            }

            val scope = rememberCoroutineScope()

            HorizontalFloatingToolbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-36).dp)
                    .zIndex(1f),
                expanded = true,
                leadingContent = {
                    // Play/Pause button
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isPlaying) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = if (isPlaying) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface
                        ),
                        onClick = {
                            if (isPlaying) {
                                isPlaying = false
                            } else {
                                isPlaying = true
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = "play/pause",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Restart button
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        onClick = {
                            isPlaying = false
                            lastPosition = null
                            playbackIndex = 0
                            animatedPath.clear()
                            revealedStayPoints.clear()
                            startLatLng?.let {
                                movingMarkerState.position = startLatLng
                            }

                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.restart),
                            contentDescription = "restart",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                content = {
                    // Tray toggle
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (showTray) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = if (showTray) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface
                        ),
                        onClick = {
                            if (showTray) showTray = false
                            else showTray = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.info),
                            contentDescription = "Toggle Tray",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Fit All markers
                    IconButton(
                        onClick = {
                            currentSession?.let {
                                scope.launch {
                                    val boundsBuilder = LatLngBounds.builder()
                                        .include(LatLng(it.startLat, it.startLng))
                                        .include(LatLng(it.endLat, it.endLng))

                                    it.stayPoints.forEach {
                                        boundsBuilder.include(LatLng(it.lat, it.lng))
                                    }


                                    val bounds = boundsBuilder.build()

                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngBounds(bounds, 230)
                                    )

                                    delay(1000)
                                    followUser = true
                                }
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.gps),
                            contentDescription = "Fit All",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Map type toggle
                    IconButton(
                        onClick = {
                            mapType = when (mapType) {
                                MapType.NORMAL -> MapType.SATELLITE
                                MapType.SATELLITE -> MapType.TERRAIN
                                MapType.TERRAIN -> MapType.HYBRID
                                else -> MapType.NORMAL
                            }
                            show = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.map_trifold),
                            contentDescription = when (mapType) {
                                MapType.NORMAL -> "Normal"
                                MapType.SATELLITE -> "Satellite"
                                MapType.TERRAIN -> "Terrain"
                                MapType.HYBRID -> "Hybrid"
                                else -> "Map"
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            )

            // Tray
            currentSession?.let { session ->

                AnimatedVisibility(
                    visible = showTray,
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    SessionPreviewTray(
                        session = session,
                        onClose = { showTray = false },
                        Modifier.padding(bottom = 32.dp)
                    )
                }

//                AnimatedVisibility(
//                    visible = !showTray,
//                    modifier = Modifier
//                        .align(Alignment.BottomStart)
//                        .padding(vertical = 32.dp, horizontal = 16.dp)
//                ) {
//                    Surface(
//                        shape = CircleShape,
//                        color = MaterialTheme.colorScheme.primaryContainer,
//                        modifier = Modifier
//                            .size(62.dp)
//                            .clickable { showTray = true }
//                    ) {
//                        Image(
//                            painter = painterResource(R.drawable.info),
//                            contentDescription = null,
//                            colorFilter = ColorFilter.tint(
//                                MaterialTheme.colorScheme.onPrimaryContainer
//                            ),
//                            modifier = Modifier.padding(12.dp)
//                        )
//                    }
//                }
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

fun interpolateLatLng(
    fraction: Float,
    start: LatLng,
    end: LatLng
): LatLng {
    val lat = (end.latitude - start.latitude) * fraction + start.latitude
    val lng = (end.longitude - start.longitude) * fraction + start.longitude
    return LatLng(lat, lng)
}

fun movingPlaybackMarkerIcon(): BitmapDescriptor {

    val size = 56
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // outer glow
    paint.color = android.graphics.Color.parseColor("#2196F3")
    paint.alpha = 80
    canvas.drawCircle(size / 2f, size / 2f, size / 2.1f, paint)

    // main circle
    paint.alpha = 255
    paint.color = android.graphics.Color.parseColor("#2196F3")
    canvas.drawCircle(size / 2f, size / 2f, size / 3f, paint)

    // white border
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 6f
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 3f, paint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}