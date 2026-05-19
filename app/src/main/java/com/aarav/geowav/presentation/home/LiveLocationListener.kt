package com.aarav.geowav.presentation.home

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingToolbarColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.UserColorMapper
import com.aarav.geowav.core.utils.ViewerLocationState
import com.aarav.geowav.core.utils.formatTime
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.presentation.components.CustomBottomSheetForObserve
import com.aarav.geowav.presentation.observe.CollapsedViewerInfo
import com.aarav.geowav.presentation.observe.CollapsedViewerInfoV2
import com.aarav.geowav.presentation.observe.CollapsedViewerTray
import com.aarav.geowav.presentation.observe.CompactActionMenu
import com.aarav.geowav.presentation.observe.ViewerInfoSheetContent
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.primaryLight
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true)
@Composable
fun ObserveLiveLocationCard(
    viewModel: HomeScreenVM,
    uiState: HomeScreenUiState,
    isFullScreen: Boolean,
    showTray: Boolean = false,
    onHideClick: () -> Unit,
    onShowTray: () -> Unit = {},
    navigateToObserve: () -> Unit,
    modifier: Modifier = Modifier,
    userLocation: Pair<Double, Double>? = null
) {
    var isUserPanning by remember { mutableStateOf(false) }

    var mapLoaded by remember { mutableStateOf(false) }

    var showMapModeToast by remember { mutableStateOf(false) }

    val markerStates = remember {
        mutableStateMapOf<String, MarkerState>()
    }

    var showViewerInfoSheet by remember {
        mutableStateOf(true)
    }


    LaunchedEffect(showMapModeToast) {
        if (showMapModeToast) {
            delay(1500)
            showMapModeToast = false
        }
    }


    val locations by viewModel.locations.collectAsState()
    val userPaths by viewModel.userPaths.collectAsState()

    val emergencyState = locations.values
        .firstOrNull { it is ViewerLocationState.EmergencySharing }
            as? ViewerLocationState.EmergencySharing


    val emergencyLat = emergencyState?.location?.lat
    val emergencyLng = emergencyState?.location?.lng


    val visibleLatLngs = locations.values
        .mapNotNull {
            when (it) {
                is ViewerLocationState.NormalSharing ->
                    LatLng(it.location.lat, it.location.lng)

                is ViewerLocationState.EmergencySharing ->
                    LatLng(it.location.lat, it.location.lng)

                ViewerLocationState.Blocked -> null
            }
        }


    val emergencyUser = locations
        .entries
        .firstOrNull { it.value is ViewerLocationState.EmergencySharing }



    val activeSharerIds = remember(locations) {
        locations.filterValues {
            it is ViewerLocationState.NormalSharing ||
                    it is ViewerLocationState.EmergencySharing
        }.keys
    }

    LaunchedEffect(activeSharerIds, mapLoaded) {
        if (!mapLoaded) return@LaunchedEffect
        if (activeSharerIds.isEmpty()) return@LaunchedEffect

        viewModel.fetchViewerInfo()
    }


    var uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false,
                compassEnabled = true,
                mapToolbarEnabled = false
            )
        )
    }

    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    val mapProperties = remember(mapType) {
        MapProperties(mapType = mapType)
    }

    val initialCameraPosition = remember {
        val emergency = locations.values
            .firstOrNull { it is ViewerLocationState.EmergencySharing }
                as? ViewerLocationState.EmergencySharing

        if (emergency != null) {
            com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                LatLng(emergency.location.lat, emergency.location.lng),
                16f
            )
        } else {
            val firstLocation = locations.values.firstNotNullOfOrNull {
                when (it) {
                    is ViewerLocationState.NormalSharing -> it.location
                    is ViewerLocationState.EmergencySharing -> it.location
                    else -> null
                }
            }

            if (firstLocation != null) {
                CameraPosition.fromLatLngZoom(
                    LatLng(firstLocation.lat, firstLocation.lng),
                    14f
                )
            } else {
                CameraPosition.fromLatLngZoom(
                    LatLng(0.0, 0.0),
                    2f
                )
            }
        }
    }


    val cameraPositionState = rememberCameraPositionState {
        position = initialCameraPosition
    }

    LaunchedEffect(activeSharerIds, mapLoaded, emergencyUser) {

        if (!mapLoaded) return@LaunchedEffect
        if (visibleLatLngs.isEmpty()) return@LaunchedEffect
        if (emergencyUser != null) return@LaunchedEffect
        if (isUserPanning) return@LaunchedEffect

        val bounds = LatLngBounds.builder().apply {
            visibleLatLngs.forEach { include(it) }
        }.build()

        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngBounds(bounds, 80)
        )
    }


    val isEmergencyActive =
        locations.values.any { it is ViewerLocationState.EmergencySharing }

    LaunchedEffect(
        isFullScreen,
        mapLoaded,
        emergencyLat,
        emergencyLng,
        isUserPanning
    ) {
        if (!mapLoaded) return@LaunchedEffect

        if (emergencyLat == null || emergencyLng == null) {
            // isFollowingEmergency = false
            return@LaunchedEffect
        }

        if (isFullScreen && !isUserPanning) {

            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(emergencyLat, emergencyLng),
                    16f
                )
            )
        }
    }


    LaunchedEffect(cameraPositionState) {
        snapshotFlow {
            cameraPositionState.isMoving to
                    cameraPositionState.cameraMoveStartedReason
        }.collect { (isMoving, reason) ->
            if (isMoving && reason == CameraMoveStartedReason.GESTURE) {
                isUserPanning = true
            } else if (!isMoving && isUserPanning) {
                delay(5000)
                isUserPanning = false
            }
        }
    }

    LaunchedEffect(isFullScreen) {
        if (isFullScreen) {
            isUserPanning = false
        }
    }


    val scope = rememberCoroutineScope()

    var selectedUser by remember {
        mutableStateOf<String?>(null)
    }

    val activeViewerIds = locations
        .filterValues {
            it is ViewerLocationState.NormalSharing ||
                    it is ViewerLocationState.EmergencySharing
        }
        .keys

    val viewers = uiState.lovedOnes.filter {
        it.id in activeViewerIds
    }


    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {

        GoogleMap(
            modifier = Modifier
                .matchParentSize()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            cameraPositionState = cameraPositionState,
            onMapClick = {},
            onMapLongClick = {},
            uiSettings = uiSettings,
            properties = mapProperties,
            onMapLoaded = { mapLoaded = true }
        ) {

            locations.forEach { (userId, state) ->

                val baseColor = UserColorMapper.getUserColor(userId).toArgb()
                val markerState = markerStates.getOrPut(userId) {
                    MarkerState()
                }
                val viewer = uiState.lovedOnes.firstOrNull { it.id == userId }

                UserMarker(
                    userId = userId,
                    markerState = markerState,
                    state = state,
                    baseColor = baseColor,
                    isSelected = selectedUser == userId,
                    avatarUrl = viewer?.avatarUrl,
                    displayName = viewer?.alias?.takeIf { it.isNotBlank() }
                        ?: viewer?.profileName
                        ?: userId
                )
            }


            userPaths.forEach { (userId, path) ->

                val colorInt = UserColorMapper.getUserColor(userId)
                val state = locations[userId]

                if (path.points.size > 1 && state != null && selectedUser == userId) {

                    val isEmergency =
                        state is ViewerLocationState.EmergencySharing

                    Polyline(
                        points = path.points.take(uiState.playbackIndex + 1),
                        color = when {
                            isEmergency ->
                                Color.Red

                            path.isActive ->
                                colorInt

                            else ->
                                Color.Gray
                        },
                        width = if (isEmergency) 10f else 6f,
                        jointType = JointType.ROUND,
                        startCap = RoundCap(),
                        endCap = RoundCap(),
                        zIndex = if (isEmergency) 1f else 0f
                    )
                }
            }

            val liveStays by viewModel.liveStayPoints.collectAsState()

            val stayIcon = remember { stayPointMarkerIcon() }

            liveStays.forEach { (_, stayPoints) ->
                stayPoints.forEach { stay ->
                    val stayPos = LatLng(stay.lat, stay.lng)

                    val mins = stay.durationMillis / 60_000
                    val durationText = if (mins < 60) "Stayed $mins min"
                    else "${mins / 60}h ${mins % 60}m"

                    Marker(
                        state = MarkerState(position = stayPos),
                        title = durationText,
                        snippet = "Stay Point",
                        anchor = Offset(0.5f, 0.5f),
                        icon = stayIcon
                    )
                }
            }

            emergencyUser?.let { (_, state) ->
                val emergencyState = state as ViewerLocationState.EmergencySharing
                val loc = emergencyState.location

                EmergencyRipple(
                    center = LatLng(loc.lat, loc.lng)
                )
            }
        }


        var lastUser by remember { mutableStateOf<String?>(null) }

        val currentPathState = userPaths[selectedUser]
        val selectedUserPath = currentPathState?.points

        LaunchedEffect(selectedUser, selectedUserPath) {

            if (selectedUserPath.isNullOrEmpty()) return@LaunchedEffect

            if (selectedUser != lastUser) {
                viewModel.resetAnimatedPath()
                lastUser = selectedUser

                // place marker at latest location
                markerStates[selectedUser]?.position = selectedUserPath.last()

                viewModel.setPlaybackIndex(selectedUserPath.size - 1)
                markerStates[selectedUser]?.position?.let {
                    viewModel.updateLastLocation(it)
                }

                return@LaunchedEffect
            }

            viewModel.drawAnimatedPath(selectedUserPath)
        }


        LaunchedEffect(uiState.lastPosition, selectedUser) {


            val location = selectedUserPath?.last()

            if (uiState.lastPosition != null) {
                markerStates[selectedUser]?.position = uiState.lastPosition
            } else {

                location?.let {
                    markerStates[selectedUser]?.position = it
                    viewModel.setPlaybackIndex(selectedUserPath.size - 1)
                }
            }

        }


        if (!isFullScreen) {
            FullScreenIcon(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clickable {
                        navigateToObserve()
                    }
            )
        }

        val mapMode = when (mapType) {
            com.google.maps.android.compose.MapType.NORMAL -> "Normal"
            com.google.maps.android.compose.MapType.HYBRID -> "Satellite"
            else -> "Map"
        }

        if(isFullScreen) {
            CompactActionMenu(
                resetCameraPosition = {
                    if (emergencyLat != null && emergencyLng != null) {
                        isUserPanning = false
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(emergencyLat, emergencyLng),
                                    16f
                                )
                            )
                        }
                    } else if (visibleLatLngs.isNotEmpty()) {
                        isUserPanning = false
                        scope.launch {
                            val bounds = LatLngBounds.builder().apply {
                                visibleLatLngs.forEach { include(it) }
                            }.build()
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngBounds(bounds, 80)
                            )
                        }
                    }
                },
                changeMapType = {
                    mapType = when (mapType) {
                        MapType.NORMAL -> MapType.HYBRID
                        else -> MapType.NORMAL
                    }
                    showMapModeToast = true
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 98.dp)
                    .padding(horizontal = 12.dp)
            )
        }

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

        when {

            showViewerInfoSheet ->
                AnimatedVisibility(
                    visible = isFullScreen,
                    enter = fadeIn(
                        animationSpec = tween(220)
                    ) + slideInVertically(
                        animationSpec = tween(260),
                        initialOffsetY = { -it / 5 }
                    ),
                    exit = fadeOut(
                        animationSpec = tween(180)
                    ) + slideOutVertically(
                        animationSpec = tween(220),
                        targetOffsetY = { -it / 6 }
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {

                    CustomBottomSheetForObserve(
                        onDismissRequest = {
                            showViewerInfoSheet = false
                        },
                        content = {

                            val selectedUserDetails =
                                uiState.lovedOnes.firstOrNull {
                                    it.id == selectedUser
                                }

                            ViewerInfoSheetContent(
                                viewers,
                                locations,
                                onClick = {
                                    selectedUser = it
                                },
                                selectedUserLocationState = locations[selectedUser],
                                selectedUserDetails = selectedUserDetails,
                                userLocation = userLocation,
                                onDismiss = {
                                    showViewerInfoSheet = false
                                    selectedUser = null
                                }
                            )
                        }
                    )
                }

            selectedUser != null ->
                AnimatedVisibility(
                    visible = isFullScreen,
                    enter = fadeIn(
                        animationSpec = tween(220)
                    ) + slideInVertically(
                        animationSpec = tween(260),
                        initialOffsetY = { -it / 5 }
                    ),
                    exit = fadeOut(
                        animationSpec = tween(160)
                    ) + slideOutVertically(
                        animationSpec = tween(180),
                        targetOffsetY = { -it / 6 }
                    ),
                    modifier = Modifier
                        .statusBarsPadding()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {

                    val member =
                        uiState.lovedOnes.firstOrNull {
                            it.id == selectedUser
                        }

                    val location = when (val state = locations[selectedUser]) {

                        is ViewerLocationState.NormalSharing ->
                            state.location

                        is ViewerLocationState.EmergencySharing ->
                            state.location

                        else -> null
                    }

                    CollapsedViewerInfo(
                        memberName = member?.alias
                            ?: member?.profileName
                            ?: "Live User",

                        lastTimestamp = location?.timestamp
                            ?: System.currentTimeMillis(),

                        onDismiss = {
                            selectedUser = null
                            showViewerInfoSheet = true
                        }
                    )
                }

            else -> CollapsedViewerTray(
                viewerInfo = viewers,
                showDetail = {
                    showViewerInfoSheet = true
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }

//        AnimatedVisibility(isFullScreen && showTray) {
//            ViewerInfoSheetContent(
//                Modifier
//                    .align(Alignment.TopCenter)
//                    .padding(top = 12.dp),
//                viewerList = uiState.lovedOnes,
//                locations = locations,
//                selectedUser = selectedUser,
//                onHideClick = {
//                    selectedUser = null
//                    onHideClick()
//                },
//                onUserClick = {
//                    selectedUser = it
//                },
//                onClick = {
//                    scope.launch {
//                        cameraPositionState.animate(
//                            CameraUpdateFactory.newLatLngZoom(
//                                it,
//                                16f
//                            )
//                        )
//                    }
//                }
//            )

//            AnimatedVisibility(
//                visible = showReplayHelp,
//                enter = fadeIn(animationSpec = tween(220)) +
//                        slideInVertically(
//                            animationSpec = tween(260),
//                            initialOffsetY = { -it / 2 }
//                        ),
//                exit = fadeOut(animationSpec = tween(160)) +
//                        slideOutVertically(
//                            animationSpec = tween(180),
//                            targetOffsetY = { -it / 6 }
//                        ),
//                modifier = Modifier
//                    .statusBarsPadding()
//                    .padding(bottom = 24.dp)
//            ) {
//                CustomBottomSheetForObserve(
//                    onDismissRequest = {
//                        showReplayHelp = false
//                    }
//                ) {
//                    ViewerInfoSheetContent()
//                }
//            }

//            AnimatedVisibility(
//                visible = !showReplayHelp,
//                enter = fadeIn(animationSpec = tween(220)) +
//                        slideInVertically(
//                            animationSpec = tween(260),
//                            initialOffsetY = { -it / 2 }
//                        ),
//                exit = fadeOut(animationSpec = tween(160)) +
//                        slideOutVertically(
//                            animationSpec = tween(180),
//                            targetOffsetY = { -it / 6 }
//                        ),
//                modifier = Modifier.align(Alignment.BottomCenter)
//            ) {
//                CollapsedViewerInfo(
//                    "Aarav",
//                    System.currentTimeMillis(),
//                    onDismiss = {
//                        showReplayHelp = true
//                    }
//                )
//            }

//        }

        if (isFullScreen) {
            val liveCount = locations.count {
                it.value is ViewerLocationState.NormalSharing ||
                        it.value is ViewerLocationState.EmergencySharing
            }

//            HorizontalFloatingToolbar(
//                colors = FloatingToolbarColors(
//                    toolbarContainerColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.85f),
//                    toolbarContentColor = MaterialTheme.colorScheme.onSurface,
//                    fabContentColor = MaterialTheme.colorScheme.onSurface,
//                    fabContainerColor = MaterialTheme.colorScheme.surfaceContainer
//                ),
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .offset(y = (-32).dp)
//                    .zIndex(1f),
//                expanded = true,
//                leadingContent = {
//                    Row(
//                        modifier = Modifier.padding(horizontal = 4.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(6.dp)
//                    ) {
//
//                        Surface(
//                            shape = RoundedCornerShape(12.dp),
//                            color = if (isEmergencyActive)
//                                MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
//                            else
//                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
//                        ) {
//                            Text(
//                                text = "$liveCount Live",
//                                fontFamily = manrope,
//                                fontWeight = FontWeight.SemiBold,
//                                fontSize = 13.sp,
//                                color = if (isEmergencyActive)
//                                    MaterialTheme.colorScheme.error
//                                else
//                                    MaterialTheme.colorScheme.primary,
//                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
//                            )
//                        }
//                    }
//                },
//                content = {
//
//                    FilledIconButton(
//                        modifier = Modifier.size(40.dp),
//                        onClick = {
//                            if (emergencyLat != null && emergencyLng != null) {
//                                isUserPanning = false
//                                scope.launch {
//                                    cameraPositionState.animate(
//                                        CameraUpdateFactory.newLatLngZoom(
//                                            LatLng(emergencyLat, emergencyLng),
//                                            16f
//                                        )
//                                    )
//                                }
//                            } else if (visibleLatLngs.isNotEmpty()) {
//                                isUserPanning = false
//                                scope.launch {
//                                    val bounds = LatLngBounds.builder().apply {
//                                        visibleLatLngs.forEach { include(it) }
//                                    }.build()
//                                    cameraPositionState.animate(
//                                        CameraUpdateFactory.newLatLngBounds(bounds, 80)
//                                    )
//                                }
//                            }
//                        },
//                        colors = IconButtonDefaults.filledIconButtonColors(
//                            containerColor = if (isEmergencyActive)
//                                MaterialTheme.colorScheme.error
//                            else
//                                MaterialTheme.colorScheme.primary,
//                            contentColor = if (isEmergencyActive)
//                                MaterialTheme.colorScheme.onError
//                            else
//                                MaterialTheme.colorScheme.onPrimary
//                        )
//                    ) {
//                        Icon(
//                            painter = painterResource(
//                                if (isEmergencyActive) R.drawable.emergency
//                                else R.drawable.gps
//                            ),
//                            contentDescription = if (isEmergencyActive)
//                                "Center on Emergency"
//                            else
//                                "Fit All Users",
//                            modifier = Modifier.size(22.dp)
//                        )
//                    }
//
//                    IconButton(
//                        modifier = Modifier.size(40.dp),
//                        onClick = {
//                            if (showTray) onHideClick() else onShowTray()
//                        }
//                    ) {
//                        Icon(
//                            painter = painterResource(R.drawable.tray),
//                            contentDescription = "Toggle Tray",
//                            modifier = Modifier.size(22.dp)
//                        )
//                    }
//
//                    IconButton(
//                        modifier = Modifier.size(40.dp),
//                        onClick = {
//                            if (visibleLatLngs.isNotEmpty()) {
//                                isUserPanning = false
//                                scope.launch {
//                                    val bounds = LatLngBounds.builder().apply {
//                                        visibleLatLngs.forEach { include(it) }
//                                    }.build()
//                                    cameraPositionState.animate(
//                                        CameraUpdateFactory.newLatLngBounds(bounds, 80)
//                                    )
//                                }
//                            }
//                        }
//                    ) {
//                        Icon(
//                            painter = painterResource(R.drawable.full_screen),
//                            contentDescription = "Fit All",
//                            modifier = Modifier.size(22.dp)
//                        )
//                    }
//
//                    IconButton(
//                        modifier = Modifier.size(40.dp),
//                        onClick = {
//                            mapType = when (mapType) {
//                                MapType.NORMAL -> MapType.SATELLITE
//                                MapType.SATELLITE -> MapType.TERRAIN
//                                MapType.TERRAIN -> MapType.HYBRID
//                                else -> MapType.NORMAL
//                            }
//                            showMapModeToast = true
//                        }
//                    ) {
//                        Icon(
//                            painter = painterResource(R.drawable.map_trifold),
//                            contentDescription = when (mapType) {
//                                MapType.NORMAL -> "Normal"
//                                MapType.SATELLITE -> "Satellite"
//                                MapType.TERRAIN -> "Terrain"
//                                MapType.HYBRID -> "Hybrid"
//                                else -> "Map"
//                            },
//                            modifier = Modifier.size(22.dp)
//                        )
//                    }
//                }
//            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RichTooltipExample(
    modifier: Modifier = Modifier,
    conn: CircleMember,
    viewerState: ViewerLocationState?,
    onClick: (LatLng) -> Unit,
    onUserClick: (String) -> Unit
) {
    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()

    val location = when (viewerState) {
        is ViewerLocationState.NormalSharing -> viewerState.location
        is ViewerLocationState.EmergencySharing -> viewerState.location
        else -> null
    }


    val context = LocalContext.current

    TooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(
            spacingBetweenTooltipAndAnchor = 24.dp
        ),
        state = tooltipState,
        tooltip = {
            RichTooltip(
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 6.dp,
                title = {
                    Text(
                        text = conn.profileName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = manrope
                        )
                    )
                },
                action = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {

                        TextButton(
                            onClick = {
                                scope.launch { tooltipState.dismiss() }
                            }
                        ) {
                            Text("Close")
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    tooltipState.dismiss()
                                    location?.let {
                                        onClick(LatLng(it.lat, it.lng))
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Show on Map")
                        }

                        IconButton(
                            onClick = {
                                scope.launch { tooltipState.dismiss() }
                                location?.let {
                                    val uri = Uri.parse("google.navigation:q=${it.lat},${it.lng}")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    intent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(intent)
                                }
                            }
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryFixed,
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.directions),
                                    contentDescription = "Directions",
                                    tint = MaterialTheme.colorScheme.onPrimaryFixed,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(6.dp)
                                )
                            }
                        }
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Text(
                        text = "${conn.profileName} is currently sharing their location with you",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = manrope,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 0.6.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Text(
                        text = "Lat: ${location?.lat}\nLng: ${location?.lng}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = manrope
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Started: ${formatTime(location?.startedAt ?: System.currentTimeMillis())}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = manrope
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Last Updated: ${formatTime(location?.timestamp ?: System.currentTimeMillis())}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = manrope
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onUserClick(conn.id)
                scope.launch {
                    tooltipState.show()
                }
            }
        ) {

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.onPrimary,
                                MaterialTheme.colorScheme.inversePrimary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conn.alias?.take(1) ?: conn.profileName.take(1),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = manrope
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = conn.alias ?: conn.profileName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = manrope,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun ViewerTrayOverlay(
    modifier: Modifier = Modifier,
    viewerList: List<CircleMember>,
    locations: Map<String, ViewerLocationState>,
    selectedUser: String?,
    onHideClick: () -> Unit,
    onUserClick: (String) -> Unit,
    onClick: (LatLng) -> Unit
) {

    val filtered = viewerList.filter {
        it.id in locations.keys
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f),
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                items(filtered, key = { it.id }) { conn ->

                    val viewerState = locations[conn.id]

                    val isSelected = selectedUser == conn.id

                    val isEmergency =
                        viewerState is ViewerLocationState.EmergencySharing

                    CompactPresenceChip(
                        conn = conn,
                        isSelected = isSelected,
                        isEmergency = isEmergency,
                        onClick = {
                            onUserClick(conn.id)

                            val location = when (viewerState) {
                                is ViewerLocationState.NormalSharing ->
                                    viewerState.location

                                is ViewerLocationState.EmergencySharing ->
                                    viewerState.location

                                else -> null
                            }

                            location?.let {
                                onClick(
                                    LatLng(it.lat, it.lng)
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onHideClick,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.clear),
                    contentDescription = "Hide Tray",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CompactPresenceChip(
    conn: CircleMember,
    isSelected: Boolean,
    isEmergency: Boolean,
    onClick: () -> Unit
) {

    val backgroundColor = when {
        isSelected ->
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)

        else ->
            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
    }

    val borderColor = when {
        isEmergency ->
            MaterialTheme.colorScheme.error.copy(alpha = 0.45f)

        isSelected ->
            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)

        else ->
            Color.Transparent
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        ),
        modifier = Modifier.clip(RoundedCornerShape(22.dp))
            .clickable {
                onClick()
            }
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 8.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Box {

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.onPrimary,
                                    MaterialTheme.colorScheme.inversePrimary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = conn.profileName.take(1),
                        fontFamily = manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (isEmergency)
                                MaterialTheme.colorScheme.error
                            else
                                Color(0xFF34C759)
                        )
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                )
            }

            Text(
                text = conn.alias ?: conn.profileName,
                fontFamily = manrope,
                fontWeight = if (isSelected)
                    FontWeight.SemiBold
                else
                    FontWeight.Medium,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ViewerCardHome(
    viewerInfo: List<CircleMember>,
    navigateToObserve: () -> Unit
) {

    val text = when {
        viewerInfo.isEmpty() ->
            "No one"

        viewerInfo.size == 1 -> {
            viewerInfo.first().profileName
        }

        viewerInfo.size == 2 ->
            viewerInfo.joinToString { it.profileName }

        else ->
            "${viewerInfo[0].profileName}, ${viewerInfo[1].profileName} + ${viewerInfo.size - 2}"
    }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Surface(
                color = Color.White,
                shape = CircleShape,
                shadowElevation = 2.dp
            ) {
                Icon(
                    painter = painterResource(R.drawable.new_logo),
                    contentDescription = null,
                    tint = primaryLight,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.width(12.dp))


            Column(
                Modifier.weight(1f),
                //verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    "Currently watching $text", fontWeight = FontWeight.SemiBold,
                    fontFamily = manrope,
                    fontSize = 14.sp,
                )
            }


            Spacer(Modifier.width(6.dp))


            val infiniteTransition = rememberInfiniteTransition()

            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Restart
                )
            )

            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.9f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Restart
                )
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .background(MaterialTheme.colorScheme.error, CircleShape)
            )
        }
    }
}


@Composable
fun EmergencyRipple(
    center: LatLng
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")

    val radius by infiniteTransition.animateFloat(
        initialValue = 20f,      // meters
        targetValue = 120f,      // meters
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )


    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    com.google.maps.android.compose.Circle(
        center = center,
        radius = radius.toDouble(),
        strokeColor = Color.Red.copy(alpha = alpha),
        fillColor = Color.Red.copy(alpha = alpha * 0.3f),
        strokeWidth = 2f,
        zIndex = 1f
    )
}


fun createAvatarMarkerBitmap(
    avatarBitmap: Bitmap?,
    displayName: String,
    isEmergency: Boolean,
    isSelected: Boolean,
    baseColor: Int
): Bitmap {
    val size = 136
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val center = size / 2f
    val avatarRadius = 44f
    val avatarBounds = RectF(
        center - avatarRadius,
        center - avatarRadius,
        center + avatarRadius,
        center + avatarRadius
    )

    if (isEmergency || isSelected) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 9f
        paint.color = if (isEmergency) android.graphics.Color.RED else baseColor
        paint.alpha = if (isEmergency) 170 else 130
        canvas.drawCircle(center, center, avatarRadius + 14f, paint)
    }

    if (isEmergency) {
        paint.style = Paint.Style.FILL
        paint.color = android.graphics.Color.RED
        paint.alpha = 45
        canvas.drawCircle(center, center, avatarRadius + 24f, paint)
    }

    paint.alpha = 255
    paint.style = Paint.Style.FILL

    if (avatarBitmap != null) {
        val clipPath = Path().apply {
            addOval(avatarBounds, Path.Direction.CW)
        }
        val source = centerCropSourceRect(avatarBitmap)

        canvas.save()
        canvas.clipPath(clipPath)
        canvas.drawBitmap(avatarBitmap, source, avatarBounds, paint)
        canvas.restore()
    } else {
        paint.color = if (isEmergency) android.graphics.Color.RED else baseColor
        canvas.drawCircle(center, center, avatarRadius, paint)

        val initial = displayName
            .trim()
            .firstOrNull()
            ?.uppercaseChar()
            ?.toString()
            ?: "?"

        paint.color = android.graphics.Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 40f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val textCenterOffset = (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(initial, center, center - textCenterOffset, paint)
    }

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 6f
    paint.color = android.graphics.Color.WHITE
    paint.alpha = 255
    canvas.drawCircle(center, center, avatarRadius, paint)

    return bitmap
}

fun markerIcon(
    isEmergency: Boolean,
    isSelected: Boolean,
    baseColor: Int,
    avatarBitmap: Bitmap?,
    displayName: String
): BitmapDescriptor {
    return BitmapDescriptorFactory.fromBitmap(
        createAvatarMarkerBitmap(
            avatarBitmap = avatarBitmap,
            displayName = displayName,
            isEmergency = isEmergency,
            isSelected = isSelected,
            baseColor = baseColor
        )
    )
}

private fun centerCropSourceRect(bitmap: Bitmap): Rect {
    val width = bitmap.width
    val height = bitmap.height

    return if (width > height) {
        val horizontalInset = (width - height) / 2
        Rect(horizontalInset, 0, horizontalInset + height, height)
    } else {
        val verticalInset = (height - width) / 2
        Rect(0, verticalInset, width, verticalInset + width)
    }
}

/**
 * Creates a distinct orange marker icon for stay points.
 */
fun stayPointMarkerIcon(): BitmapDescriptor {
    val size = 64
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
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
    paint.strokeWidth = 4f
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 3f, paint)

    // Inner dot
    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 8f, paint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}


@Composable
fun UserMarker(
    userId: String,
    markerState: MarkerState,
    state: ViewerLocationState,
    baseColor: Int,
    isSelected: Boolean,
    avatarUrl: String?,
    displayName: String
) {
    if (state is ViewerLocationState.Blocked) return

    val context = LocalContext.current
    var avatarBitmap by remember(userId) {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(avatarUrl) {
        avatarBitmap = null

        val url = avatarUrl?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()

        avatarBitmap = try {
            val result = context.imageLoader.execute(request)
            (result as? SuccessResult)
                ?.drawable
                ?.toBitmap()
        } catch (e: Exception) {
            null
        }
    }

    val location = when (state) {
        is ViewerLocationState.NormalSharing -> state.location
        is ViewerLocationState.EmergencySharing -> state.location
        else -> return
    }

    val target = LatLng(location.lat, location.lng)

     if (!isSelected) {
        LaunchedEffect(target) {
            val start = markerState.position

            if (start.latitude == 0.0 && start.longitude == 0.0) {
                markerState.position = target
                return@LaunchedEffect
            }

            val steps = 45
            val stepDelay = 16L 

            for (i in 1..steps) {
                val t = i.toFloat() / steps
                val eased = 1f - (1f - t).let { it * it * it }

                markerState.position = LatLng(
                    start.latitude + (target.latitude - start.latitude) * eased,
                    start.longitude + (target.longitude - start.longitude) * eased
                )
                delay(stepDelay)
            }
            markerState.position = target
        }
    }

    val isEmergency = state is ViewerLocationState.EmergencySharing

    val cachedIcon = remember(
        isEmergency,
        isSelected,
        baseColor,
        avatarBitmap,
        displayName
    ) {
        markerIcon(
            isEmergency = isEmergency,
            isSelected = isSelected,
            baseColor = baseColor,
            avatarBitmap = avatarBitmap,
            displayName = displayName
        )
    }

    Marker(
        state = markerState,
        icon = cachedIcon,
        anchor = Offset(0.5f, 0.5f),
        zIndex = if (isEmergency) 2f else 1f
    )
}


@Preview
@Composable
fun FullScreenIcon(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
                )
        )

        Icon(
            painter = painterResource(R.drawable.full_screen),
            contentDescription = "full_screen",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.Center)
        )
    }

}
