package com.aarav.geowav.presentation.timeline

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.FeatureAccess
import com.aarav.geowav.data.model.StayPoint
import com.aarav.geowav.data.model.TimelineItem
import com.aarav.geowav.data.model.UpgradeContext
import com.aarav.geowav.data.model.UpgradeEvents
import com.aarav.geowav.data.model.UpgradeReason
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.data.model.toLatLng
import com.aarav.geowav.presentation.components.CustomBottomSheet
import com.aarav.geowav.presentation.components.UpgradeBottomSheetContent
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.GeoWavTheme
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.onBackgroundDark
import com.aarav.geowav.presentation.theme.onPrimaryLight
import com.aarav.geowav.presentation.theme.outlineLight
import com.aarav.geowav.presentation.theme.outlineVariantLight
import com.aarav.geowav.presentation.theme.primaryLight
import com.aarav.geowav.presentation.theme.surfaceContainerDark
import com.aarav.geowav.presentation.theme.surfaceLight
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimelineMapPreview(
    subscriptionViewModel: SubscriptionViewModel,
    viewModel: TimelineMapPreviewVM,
    back: () -> Unit,
    navigateToPaywall: () -> Unit,
    sessionId: String,
    userId: String
) {

    val plan by subscriptionViewModel.userPlan.collectAsState()

    val uiState by viewModel.uiState.collectAsState()

    val animatedPath by viewModel.animatedPath.collectAsState()
    val lastPosition by viewModel.lastPosition.collectAsState()

//    var showUpgradeForStayPoints by rememberSaveable {
//        mutableStateOf(true)
//    }


    var showPlaybackSpeedControls by remember {
        mutableStateOf(false)
    }

    var followUser by remember {
        mutableStateOf(true)
    }
    var mapLoaded by remember {
        mutableStateOf(false)
    }
    var showTray by remember {
        mutableStateOf(true)
    }
    var showMapModeToast by remember {
        mutableStateOf(false)
    }

    val movingMarkerState = remember {
        MarkerState()
    }
    val startMarkerState = remember {
        MarkerState()
    }
    val endMarkerState = remember {
        MarkerState()
    }

    val startIcon = remember(mapLoaded) {
        if (mapLoaded) timelineMarkerIcon(Color(0xFF515B92), true) else null
    }
    val endIcon = remember(mapLoaded) {
        if (mapLoaded) timelineMarkerIcon(Color(0xFF904A44), false) else null
    }
    val movingIcon = remember(mapLoaded) {
        if (mapLoaded) movingPlaybackMarkerIcon() else null
    }
    val stayIcon = remember(mapLoaded) {
        if (mapLoaded) replayStayPointMarkerIcon() else null
    }

    val currentSession = uiState.session
    val snappedPath = uiState.snappedPath

    val startLatLng = remember(currentSession?.id) {
        currentSession?.let { LatLng(it.startLat, it.startLng) }
    }

    val userPaths = remember(currentSession) {
        currentSession?.userPath?.map { it.toLatLng() }
    }

    val finalSnappedPath = remember(snappedPath) {
        snappedPath.map { LatLng(it.location.latitude, it.location.longitude) }
    }

    var upgradeContext by remember { mutableStateOf<UpgradeContext?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            if (event is UpgradeEvents.ShowUpgrade) {
                upgradeContext = event.upgradeContext
            }
        }
    }

    upgradeContext?.let {
        CustomBottomSheet(
            onDismissRequest = {
                upgradeContext = null
            }
        ) {
            UpgradeBottomSheetContent(
                context = it,
                onUpgradeClick = {
                    upgradeContext = null
                    navigateToPaywall()
                },
                onDismiss = { upgradeContext = null }
            )
        }
    }


    LaunchedEffect(sessionId) {
        viewModel.getSessionInfo(sessionId, userId)
    }

    LaunchedEffect(userPaths) {
        if (!userPaths.isNullOrEmpty()) {
            viewModel.getSnappedPath(userPaths, true)
        }
    }

    val cameraPositionState = rememberCameraPositionState()


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

    LaunchedEffect(lastPosition) {
        lastPosition?.let { pos ->
            movingMarkerState.position = pos
            if (followUser) {
                cameraPositionState.move(CameraUpdateFactory.newLatLng(pos))
            }
        }
    }

    LaunchedEffect(uiState.isPlaying) {
        if (uiState.isPlaying && finalSnappedPath.isNotEmpty()
        ) {
            viewModel.runPlayback(
                userPlan = plan,
                path = finalSnappedPath,
                stayPoints = currentSession?.stayPoints ?: emptyList()
            )
        }
    }


    LaunchedEffect(showMapModeToast) {
        if (showMapModeToast) {
            delay(1500)
            showMapModeToast = false
        }
    }


    LaunchedEffect(currentSession?.id) {
        currentSession?.let {
            startMarkerState.position = LatLng(it.startLat, it.startLng)
            endMarkerState.position = LatLng(it.endLat, it.endLng)
        }
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(mapLoaded) {
        if (mapLoaded && currentSession != null) {
            val boundsBuilder = LatLngBounds.builder()
                .include(LatLng(currentSession.startLat, currentSession.startLng))
                .include(LatLng(currentSession.endLat, currentSession.endLng))

            currentSession.stayPoints.forEach { stay ->
                boundsBuilder.include(LatLng(stay.lat, stay.lng))
            }

            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(
                    boundsBuilder.build(), 300
                )
            )
        }
    }

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
                    IconButton(onClick = back) {
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
//
//        Log.i("SNAP", "path: $finalSnappedPath")

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
                    mapType = uiState.mapType
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    compassEnabled = true,
                    mapToolbarEnabled = false
                ),
                onMapLoaded = { mapLoaded = true }
            ) {

                currentSession?.let { session ->

                    val start = remember(session.id) { LatLng(session.startLat, session.startLng) }
                    val end = remember(session.id) { LatLng(session.endLat, session.endLng) }

                    startIcon?.let {
                        Marker(
                            state = startMarkerState,
                            icon = it,
                            title = "Start: ${session.startAddress}",
                            anchor = Offset(0.5f, 0.5f)
                        )
                    }


                    endIcon?.let {
                        Marker(
                            state = endMarkerState,
                            icon = it,
                            title = "End: ${session.endAddress}",
                            anchor = Offset(0.5f, 0.5f)
                        )
                    }


                    if (uiState.playbackIndex != 0 || lastPosition != null) {
                        Marker(
                            state = movingMarkerState,
                            icon = movingIcon,
                            anchor = Offset(0.5f, 0.5f),
                            title = currentSession.name
                        )
                    }


                    if (finalSnappedPath.isNotEmpty()) {
                        RoutePreviewPath(
                            fullPath = finalSnappedPath,
                            activePath = animatedPath.toList()
                        )
                    }


                    if (plan == UserPlan.PRO) {
                        if (uiState.revealedStayPoints.isNotEmpty()) {
                            uiState.revealedStayPoints.forEach { stay ->
                                StayPointMarker(stay, stayIcon)
                            }
                        } else {
                            if (!uiState.isPlaying && lastPosition == null) {
                                session.stayPoints.forEach { stay ->
                                    StayPointMarker(stay, stayIcon)
                                }
                            }
                        }
                    }
                }
            }

            val mapMode = when (uiState.mapType) {
                com.google.maps.android.compose.MapType.NORMAL -> "Normal"
                com.google.maps.android.compose.MapType.SATELLITE -> "Satellite"
                com.google.maps.android.compose.MapType.TERRAIN -> "Terrain"
                com.google.maps.android.compose.MapType.HYBRID -> "Hybrid"
                else -> "Map"
            }

            AnimatedVisibility(
                showMapModeToast,
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

            PlaybackDock(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-32).dp)
                    .padding(horizontal = 16.dp),
                isPlaying = uiState.isPlaying,
                canUsePlayback = FeatureAccess.canUsePlayback(plan),
                canControlSpeed = FeatureAccess.canControlSpeed(plan),
                progress = if (finalSnappedPath.size > 1) {
                    uiState.playbackIndex / (finalSnappedPath.lastIndex).toFloat()
                } else 0f,
                speedLabel = when (uiState.speed) {
                    2f -> "0.5x"
                    5f -> "1x"
                    8f -> "2x"
                    else -> "1x"
                },
                updateSpeed = {
                    if (!FeatureAccess.canControlSpeed(plan)) {
                        upgradeContext = UpgradeContext(
                            upgradeTo = UserPlan.PREMIUM,
                            reason = UpgradeReason.SpeedControl
                        )
                    } else {
                        viewModel.updateSpeed(it)
                    }
                },
                showTray = showTray,
                mapMode = mapMode,
                onPlayPause = {
                    if (uiState.isPlaying) {
                        viewModel.pausePlayback()
                    } else {
                        viewModel.startPlayback(plan)
                    }
                },
                onRestart = {
                    startLatLng?.let { viewModel.restartPlayback(it) }
                },
                onToggleTray = { showTray = !showTray },
                onFitRoute = {
                    currentSession?.let { session ->
                        scope.launch {
                            val boundsBuilder = LatLngBounds.builder()
                                .include(LatLng(session.startLat, session.startLng))
                                .include(LatLng(session.endLat, session.endLng))

                            session.stayPoints.forEach {
                                boundsBuilder.include(LatLng(it.lat, it.lng))
                            }

                            if (uiState.isPlaying && lastPosition != null) {
                                lastPosition?.let {
                                    boundsBuilder.include(it)
                                }
                            }

                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngBounds(
                                    boundsBuilder.build(), 200
                                )
                            )

                            delay(1000)
                            followUser = true
                        }
                    }
                },
                onMapMode = {
                    viewModel.toggleMapType()
                    showMapModeToast = true
                }
            )

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
            }
        }
    }
}


@Composable
private fun RoutePreviewPath(
    fullPath: List<LatLng>,
    activePath: List<LatLng>
) {
    if (fullPath.size < 2) return

    Polyline(
        points = fullPath,
        color = Color(0xFF102B35).copy(alpha = 0.34f),
        width = 9f
    )

    Polyline(
        points = fullPath,
        color = Color.White.copy(alpha = 0.38f),
        width = 4f
    )

    if (activePath.size >= 2) {
        Polyline(
            points = activePath,
            color = Color(0xFF8FD8EA),
            width = 7f
        )

        Polyline(
            points = activePath,
            color = Color.White.copy(alpha = 0.72f),
            width = 2.5f
        )
    }
}

@Composable
private fun PlaybackDock(
    isPlaying: Boolean,
    canUsePlayback: Boolean,
    canControlSpeed: Boolean,
    progress: Float,
    speedLabel: String,
    showTray: Boolean,
    mapMode: String,
    onPlayPause: () -> Unit,
    onRestart: () -> Unit,
    updateSpeed: (Float) -> Unit,
    onToggleTray: () -> Unit,
    onFitRoute: () -> Unit,
    onMapMode: () -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xEE111820),
        tonalElevation = 0.dp,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = if (isPlaying) "Replaying movement" else "Journey replay",
                        fontFamily = manrope,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(99.dp)),
                        color = Color(0xFF8FD8EA),
                        trackColor = Color.White.copy(alpha = 0.16f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = Color.White.copy(alpha = 0.10f),
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                val nextSpeed = when (speedLabel) {
                                    "0.5x" -> 5f
                                    "1x" -> 8f
                                    "2x" -> 2f
                                    else -> 5f
                                }

                                updateSpeed(nextSpeed)
                            }
                        )
                ) {
                    Text(
                        text = speedLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontFamily = manrope,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = if (canControlSpeed) 0.90f else 0.45f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DockIconButton(
                    onClick = onPlayPause,
                    containerColor = if (isPlaying) Color(0xFF8FD8EA) else Color.White,
                    contentColor = Color(0xFF101820),
                    size = 48.dp
                ) {
                    LockedIcon(
                        isLocked = !canUsePlayback,
                        icon = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play_v2),
                        contentDescription = "play/pause",
                        iconSize = 24.dp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DockIconButton(onClick = onRestart) {
                        Icon(
                            painter = painterResource(R.drawable.restart),
                            contentDescription = "restart",
                            modifier = Modifier.size(21.dp)
                        )
                    }

//                    DockIconButton(onClick = onSpeed) {
//                        LockedIcon(
//                            isLocked = !canControlSpeed,
//                            icon = painterResource(R.drawable.playback_speed),
//                            contentDescription = "speed",
//                            iconSize = 21.dp
//                        )
//                    }

                    DockIconButton(
                        onClick = onToggleTray,
                        containerColor = if (showTray) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.10f),
                        contentColor = Color.White
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.info),
                            contentDescription = "Toggle Tray",
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    DockIconButton(
                        onClick = onFitRoute,
                        containerColor = Color(0xFF8FD8EA),
                        contentColor = Color(0xFF101820)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.gps),
                            contentDescription = "Fit All",
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    DockIconButton(onClick = onMapMode) {
                        Icon(
                            painter = painterResource(R.drawable.map_trifold),
                            contentDescription = mapMode,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DockIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 40.dp,
    containerColor: Color = Color.White.copy(alpha = 0.10f),
    contentColor: Color = Color.White.copy(alpha = if (enabled) 0.92f else 0.42f),
    content: @Composable () -> Unit
) {
    IconButton(
        modifier = modifier.size(size),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = Color.White.copy(alpha = 0.08f),
            disabledContentColor = Color.White.copy(alpha = 0.42f)
        ),
        onClick = onClick
    ) {
        content()
    }
}

@Composable
private fun StayPointMarker(stay: StayPoint, icon: BitmapDescriptor?) {

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
        icon = icon,
        title = durationText,
        snippet = "$startStr – $endStr",
        anchor = Offset(0.5f, 0.5f)
    )
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
                )
            }


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
                        color = Color(0xFFFF9800)
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

@Composable
fun LockedIcon(
    isLocked: Boolean,
    icon: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    iconSize: Dp = 22.dp,
    badgeSize: Dp = 16.dp,
    lockIconSize: Dp = 10.dp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        Icon(
            painter = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize)
        )

        if (isLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(badgeSize)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.lock_fill),
                    contentDescription = null,
                    modifier = Modifier.size(lockIconSize),
                    tint = MaterialTheme.colorScheme.onSurface
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
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)


    paint.color = color
    paint.alpha = 60
    canvas.drawCircle(size / 2f, size / 2f, size / 2.2f, paint)


    paint.alpha = 255
    paint.color = color
    canvas.drawCircle(size / 2f, size / 2f, size / 3f, paint)


    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 6f
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 3f, paint)

    return bitmap
}

fun timelineMarkerIcon(
    color: Color,
    isStart: Boolean
): BitmapDescriptor {
    return BitmapDescriptorFactory.fromBitmap(
        createTimelineMarkerBitmap(
            color = color.toArgb(),
            isStart = isStart
        )
    )
}

fun replayStayPointMarkerIcon(): BitmapDescriptor {
    val size = 72
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)


    paint.color = android.graphics.Color.parseColor("#FF9800")
    paint.alpha = 60
    canvas.drawCircle(size / 2f, size / 2f, size / 2.2f, paint)


    paint.alpha = 255
    paint.color = android.graphics.Color.parseColor("#FF9800")
    canvas.drawCircle(size / 2f, size / 2f, size / 3f, paint)


    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 5f
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 3f, paint)


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


    paint.color = android.graphics.Color.parseColor("#2196F3")
    paint.alpha = 80
    canvas.drawCircle(size / 2f, size / 2f, size / 2.1f, paint)


    paint.alpha = 255
    paint.color = android.graphics.Color.parseColor("#2196F3")
    canvas.drawCircle(size / 2f, size / 2f, size / 3f, paint)


    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 6f
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 3f, paint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

enum class Speed(val value: Float, val label: String) {
    LOW(1f, "0.5x"),
    MEDIUM(2f, "1x"),
    HIGH(3f, "2x")
}
