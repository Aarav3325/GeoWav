package com.aarav.geowav.presentation.timeline

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
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
        if (mapLoaded) timelineMarkerIcon(Color(0xFF8FD8EA), true) else null
    }
    val endIcon = remember(mapLoaded) {
        if (mapLoaded) timelineMarkerIcon(Color(0xFFFFB4A9), false) else null
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
//
//        Log.i("SNAP", "path: $finalSnappedPath")

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            GoogleMap(
                modifier = Modifier
                    .fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = uiState.mapType
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    compassEnabled = false,
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

            val hasRecordedMovement = !userPaths.isNullOrEmpty()
            val isPreparingRoute = mapLoaded && currentSession != null &&
                    hasRecordedMovement && finalSnappedPath.isEmpty()
            val hasNoMovementData = mapLoaded && currentSession != null && !hasRecordedMovement

            when {
                !mapLoaded -> {
                    ReplayStatusOverlay(
                        title = "Preparing replay",
                        message = "Loading the map and route context",
                        showLoader = true,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                isPreparingRoute -> {
                    ReplayStatusOverlay(
                        title = "Refining route",
                        message = "Preparing movement playback",
                        showLoader = true,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                hasNoMovementData -> {
                    ReplayStatusOverlay(
                        title = "No movement to replay",
                        message = "This session does not include enough route data",
                        showLoader = false,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            val mapMode = when (uiState.mapType) {
                com.google.maps.android.compose.MapType.NORMAL -> "Normal"
                com.google.maps.android.compose.MapType.HYBRID -> "Satellite"
                else -> "Map"
            }

            SessionPreviewTopBar(
                onBack = back,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 16.dp, top = 10.dp)
            )

            AnimatedVisibility(
                showMapModeToast,
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xDD111820),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = "$mapMode map",
                        color = Color.White.copy(alpha = 0.86f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
                    )
                }
            }

            PlaybackDock(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-32).dp)
                    .padding(horizontal = 16.dp),
                isPlaying = uiState.isPlaying,
                isComplete = finalSnappedPath.size > 1 &&
                        uiState.playbackIndex >= finalSnappedPath.lastIndex &&
                        !uiState.isPlaying,
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
                    if (finalSnappedPath.size > 1 &&
                        uiState.playbackIndex >= finalSnappedPath.lastIndex &&
                        !uiState.isPlaying
                    ) {
                        startLatLng?.let { viewModel.restartPlayback(it) }
                    } else if (uiState.isPlaying) {
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
                    enter = fadeIn(animationSpec = tween(220)) +
                            slideInVertically(
                                animationSpec = tween(260),
                                initialOffsetY = { -it / 5 }
                            ),
                    exit = fadeOut(animationSpec = tween(160)) +
                            slideOutVertically(
                                animationSpec = tween(180),
                                targetOffsetY = { -it / 6 }
                            ),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 62.dp)
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
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ReplayStatusOverlay(
    title: String,
    message: String,
    showLoader: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(horizontal = 32.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xEE111820),
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showLoader) {
                LoadingIndicator(
                    modifier = Modifier.size(34.dp)
                )
            }

            Text(
                text = title,
                fontFamily = manrope,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Text(
                text = message,
                fontFamily = manrope,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.62f)
            )
        }
    }
}


@Composable
private fun SessionPreviewTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconButton(
            modifier = Modifier.size(42.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color(0xCC111820),
                contentColor = Color.White.copy(alpha = 0.88f)
            ),
            onClick = onBack
        ) {
            Icon(
                painter = painterResource(R.drawable.back),
                contentDescription = "Back",
                tint = Color.White.copy(alpha = 0.84f),
                modifier = Modifier.size(21.dp).align(Alignment.CenterVertically)
            )
        }

        Surface(
            shape = RoundedCornerShape(99.dp),
            color = Color(0xCC111820)
        ) {
            Text(
                text = "Session replay",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                fontFamily = manrope,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.84f)
            )
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
        color = Color(0xFF071116).copy(alpha = 0.88f),
        width = 13f
    )

    Polyline(
        points = fullPath,
        color = Color.White.copy(alpha = 0.78f),
        width = 7f
    )

    if (activePath.size >= 2) {
        Polyline(
            points = activePath,
            color = Color(0xFF071116).copy(alpha = 0.92f),
            width = 15f
        )

        Polyline(
            points = activePath,
            color = Color(0xFF19C7E6),
            width = 10f
        )

        Polyline(
            points = activePath,
            color = Color.White.copy(alpha = 0.92f),
            width = 3.5f
        )
    }
}

@Composable
private fun PlaybackDock(
    isPlaying: Boolean,
    isComplete: Boolean,
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
                        text = when {
                            isComplete -> "Replay complete"
                            isPlaying -> "Replaying movement"
                            else -> "Journey replay"
                        },
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
                    containerColor = if (isPlaying || isComplete) Color(0xFF8FD8EA) else Color.White,
                    contentColor = Color(0xFF101820),
                    size = 48.dp
                ) {
                    LockedIcon(
                        isLocked = !canUsePlayback,
                        icon = painterResource(
                            when {
                                isPlaying -> R.drawable.pause
                                isComplete -> R.drawable.restart
                                else -> R.drawable.play_v2
                            }
                        ),
                        contentDescription = if (isComplete) "restart" else "play/pause",
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

    val startLocation = remember(session.startAddress) {
        compactTimelineLocation(session.startAddress)
    }
    val endLocation = remember(session.endAddress) {
        compactTimelineLocation(session.endAddress)
    }
    val stayCount = session.stayPoints.size
    val totalStayMins = session.stayPoints.sumOf { it.durationMillis } / 60_000
    val totalStayText = if (totalStayMins < 60) "$totalStayMins min"
    else "${totalStayMins / 60}h ${totalStayMins % 60}m"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xEE111820),
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = session.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "$date / $durationText journey",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = manrope,
                        color = Color.White.copy(alpha = 0.62f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    modifier = Modifier.size(34.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        contentColor = Color.White.copy(alpha = 0.78f)
                    ),
                    onClick = onClose
                ) {
                    Icon(
                        painter = painterResource(R.drawable.clear),
                        contentDescription = "Close",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.10f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SessionEndpointSummary(
                    label = "Start",
                    time = startTime,
                    location = startLocation,
                    accentColor = Color(0xFF8FD8EA),
                    modifier = Modifier.weight(1f)
                )

                SessionEndpointSummary(
                    label = "End",
                    time = endTime,
                    location = endLocation,
                    accentColor = Color(0xFFFFB4A9),
                    modifier = Modifier.weight(1f)
                )
            }


            if (stayCount > 0) {
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = Color.White.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = "$stayCount stop${if (stayCount > 1) "s" else ""} / $totalStayText paused",
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFD39B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionEndpointSummary(
    label: String,
    time: String,
    location: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$label / $time",
            style = MaterialTheme.typography.labelMedium,
            fontFamily = manrope,
            fontWeight = FontWeight.SemiBold,
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = location,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = manrope,
            color = Color.White.copy(alpha = 0.82f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun compactTimelineLocation(address: String): String {
    val parts = address
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    return parts.take(2).joinToString(", ").ifBlank { address }
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

    val size = 92
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val center = size / 2f

    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.parseColor("#101820")
    paint.alpha = 180
    canvas.drawCircle(center, center, size / 2.55f, paint)


    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 7f
    paint.color = color
    paint.alpha = 255
    canvas.drawCircle(center, center, size / 3.15f, paint)


    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.WHITE
    paint.alpha = 245
    canvas.drawCircle(center, center, size / 4.15f, paint)

    paint.color = color
    paint.alpha = 255
    canvas.drawCircle(center, center, size / 9.5f, paint)

    paint.color = android.graphics.Color.parseColor("#101820")
    paint.textAlign = Paint.Align.CENTER
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 22f
    val label = if (isStart) "S" else "E"
    val textCenterOffset = (paint.descent() + paint.ascent()) / 2f
    canvas.drawText(label, center, center - textCenterOffset, paint)

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
    val size = 78
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val center = size / 2f

    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.parseColor("#101820")
    paint.alpha = 150
    canvas.drawCircle(center, center, size / 2.6f, paint)


    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 6f
    paint.color = android.graphics.Color.parseColor("#FFD39B")
    paint.alpha = 245
    canvas.drawCircle(center, center, size / 3.2f, paint)


    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.WHITE
    paint.alpha = 235
    canvas.drawCircle(center, center, size / 4.35f, paint)


    paint.color = android.graphics.Color.parseColor("#101820")
    paint.alpha = 255
    val barWidth = size / 13f
    val barHeight = size / 4.8f
    val gap = size / 16f
    canvas.drawRoundRect(
        center - gap - barWidth,
        center - barHeight / 2f,
        center - gap,
        center + barHeight / 2f,
        barWidth / 2f,
        barWidth / 2f,
        paint
    )
    canvas.drawRoundRect(
        center + gap,
        center - barHeight / 2f,
        center + gap + barWidth,
        center + barHeight / 2f,
        barWidth / 2f,
        barWidth / 2f,
        paint
    )

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

    val size = 96
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val center = size / 2f

    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.parseColor("#071116")
    paint.alpha = 185
    canvas.drawCircle(center, center, size / 2.55f, paint)

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 7f
    paint.color = android.graphics.Color.WHITE
    paint.alpha = 245
    canvas.drawCircle(center, center, size / 3.15f, paint)

    paint.strokeWidth = 5f
    paint.color = android.graphics.Color.parseColor("#19C7E6")
    paint.alpha = 255
    canvas.drawCircle(center, center, size / 3.75f, paint)

    paint.style = Paint.Style.FILL
    paint.alpha = 255
    paint.color = android.graphics.Color.parseColor("#19C7E6")
    canvas.drawCircle(center, center, size / 5.1f, paint)

    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(center, center, size / 11.5f, paint)

    paint.color = android.graphics.Color.parseColor("#071116")
    paint.alpha = 230
    canvas.drawCircle(center, center, size / 20f, paint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

enum class Speed(val value: Float, val label: String) {
    LOW(1f, "0.5x"),
    MEDIUM(2f, "1x"),
    HIGH(3f, "2x")
}
