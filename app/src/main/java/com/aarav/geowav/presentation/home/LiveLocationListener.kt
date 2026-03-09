package com.aarav.geowav.presentation.home

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.ViewerLocationState
import com.aarav.geowav.core.utils.formatTime
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.timeline.movingPlaybackMarkerIcon
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
import kotlinx.coroutines.coroutineScope
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
    modifier: Modifier = Modifier
) {
    var isUserPanning by remember { mutableStateOf(false) }

    var mapLoaded by remember { mutableStateOf(false) }

    var showMapModeToast by remember { mutableStateOf(false) }

    val markerStates = remember {
        mutableStateMapOf<String, MarkerState>()
    }

    val movingIcon = remember(mapLoaded) {
        if (mapLoaded) movingPlaybackMarkerIcon() else null
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

    //    val cameraPositionState = rememberCameraPositionState()

    //    Card(
    //        shape = RoundedCornerShape(16.dp),
    //        colors = CardDefaults.cardColors(
    //            containerColor = MaterialTheme.colorScheme.secondaryContainer
    //        ),
    //        modifier = Modifier
    //            .padding(top = 16.dp)
    //            .fillMaxWidth()
    //            .height(220.dp)
    //    ) {


    // Get list of users who are currently sharing location
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


    // Find if any user is in emergency state
    val emergencyUser = locations
        .entries
        .firstOrNull { it.value is ViewerLocationState.EmergencySharing }



    LaunchedEffect(visibleLatLngs, mapLoaded) {
        if (!mapLoaded) return@LaunchedEffect

        if (visibleLatLngs.isEmpty()) return@LaunchedEffect

        viewModel.fetchViewerInfo()
    }

    val viewerInfo = uiState.currentViewers


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

    val initialCameraPosition = remember(locations) {
        val emergency = locations.values
            .firstOrNull { it is ViewerLocationState.EmergencySharing }
                as? ViewerLocationState.EmergencySharing

        if (emergency != null) {
            com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                LatLng(emergency.location.lat, emergency.location.lng),
                16f
            )
        } else {
//            com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
//                LatLng(20.0, 78.0),
//                5f
//            )
            CameraPosition.fromLatLngZoom(
                LatLng(47.6677146, -122.3470447),
                5f
            )
        }
    }


    val cameraPositionState = rememberCameraPositionState {
        position = initialCameraPosition
    }


    /* Calculate a rectangle that contains all currently visible users, then smoothly
     move and zoom the map camera so everyone fits nicely inside the card*/
    // Normal mode camera behavior - show all visible users on map
//    LaunchedEffect(visibleLatLngs, mapLoaded, emergencyUser) {
//        if (!mapLoaded) return@LaunchedEffect // no camera animation until map loads
//        if (visibleLatLngs.isEmpty()) return@LaunchedEffect // nothing to show on map
//
//        if (emergencyUser != null) return@LaunchedEffect
//
//        isUserPanning = false
//
//        // Draws an invisible rectangle on map to show all visible users (zoom so that all users are visible)
//        val bounds = LatLngBounds.builder().apply {
//            visibleLatLngs.forEach { include(it) } // use include to add latlng to bounds
//        }.build()
//        // The smallest map rectangle that contains all visible users
//
//        /* Move the camera so that this bounds fits completely inside the screen,
//           Camera auto-zooms until all dots are visible. */
//        cameraPositionState.animate(
//            CameraUpdateFactory.newLatLngBounds(bounds, 80)
//        )
//    }
    LaunchedEffect(visibleLatLngs, mapLoaded, emergencyUser) {

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

    //
    //    LaunchedEffect(emergencyLocation) {
    //        if (emergencyLocation != null) {
    //            cameraMode = CameraMode.EMERGENCY_AUTO
    //        }
    //    }


    val isEmergencyActive =
        locations.values.any { it is ViewerLocationState.EmergencySharing }


//        LaunchedEffect(emergencyLocation, mapLoaded) {
//
//            if (!mapLoaded) return@LaunchedEffect
//            if (emergencyLocation == null) {
//                return@LaunchedEffect
//            }
//            if(isUserPanning) return@LaunchedEffect
//
//    //        if (cameraMode == CameraMode.USER_MANUAL) return@LaunchedEffect
//
//            // First time → move instantly (guaranteed)
//            cameraPositionState.move(
//                CameraUpdateFactory.newLatLngZoom(
//                    LatLng(emergencyLocation.lat, emergencyLocation.lng),
//                    16f
//                )
//            )
//        }

    //    LaunchedEffect(emergencyLocation) {
    //        if (emergencyLocation == null) {
    //            cameraMode = CameraMode.NORMAL
    //        }
    //    }
//
//    LaunchedEffect(isFullScreen, mapLoaded) {
//        if (!mapLoaded) return@LaunchedEffect
//        if (!isFullScreen) return@LaunchedEffect
//        if (emergencyLat == null || emergencyLng == null) return@LaunchedEffect
//
//        isUserPanning = false
//
//        cameraPositionState.move(
//            CameraUpdateFactory.newLatLngZoom(
//                LatLng(emergencyLat, emergencyLng),
//                16f
//            )
//        )
//    }

//
//        /* “When an emergency is active, automatically focus the camera on the emergency user
//        unless the user manually moved the map
//     */
//        LaunchedEffect(emergencyUser, mapLoaded, isFullScreen) {
//            if (!mapLoaded) return@LaunchedEffect // no animations before map is loaded
//            if (emergencyUser == null) {
//                userMovedCamera = false
//                return@LaunchedEffect
//            } /*
//                    if no emergency is active then normal camera logic will handle things
//                */
//
////            if(isUserPanning) return@LaunchedEffect
//
//            val state = emergencyUser.value as ViewerLocationState.EmergencySharing
//            val loc = state.location
//
//            cameraPositionState.animate(
//                CameraUpdateFactory.newLatLngZoom(
//                    LatLng(loc.lat, loc.lng),
//                    16f
//                )
//            )
//        }

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

        // Emergency just started → force follow
//        if (!isFollowingEmergency) {
//            isFollowingEmergency = true
//            isUserPanning = false
//        }

        if (isFullScreen && !isUserPanning) {

            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(emergencyLat, emergencyLng),
                    16f
                )
            )
        }

        /*
        // If entering fullscreen while emergency active → force follow
        if (isFullScreen) {
            isUserPanning = false
            isFollowingEmergency = true
        }

        // Only follow if user is not panning
        if (!isUserPanning) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(emergencyLat, emergencyLng),
                    16f
                )
            )
        }
        */
    }


//    LaunchedEffect(mapLoaded, emergencyLat, emergencyLng, isFullScreen, isUserPanning, isFollowingEmergency) {
//
//        if (!mapLoaded) return@LaunchedEffect
//        if (emergencyLat == null || emergencyLng == null) {
//            isFollowingEmergency = false
//            return@LaunchedEffect
//        }
//
//        // When entering fullscreen during emergency → enable follow
//        if (isFullScreen && !isFollowingEmergency) {
//            isFollowingEmergency = true
//            isUserPanning = false
//        }
//
//        // Follow ONLY if fullscreen AND not panning AND following is enabled
//        if (isFullScreen && !isUserPanning && isFollowingEmergency) {
//            cameraPositionState.animate(
//                CameraUpdateFactory.newLatLngZoom(
//                    LatLng(emergencyLat, emergencyLng),
//                    16f
//                )
//            )
//        }
//    }


    // Detect when user manually moves the map
    LaunchedEffect(cameraPositionState) {
        snapshotFlow {
            cameraPositionState.isMoving to
                    cameraPositionState.cameraMoveStartedReason
        }.collect { (isMoving, reason) ->
            if (isMoving && reason == CameraMoveStartedReason.GESTURE) {
                isUserPanning = true
            }
        }
    }

    LaunchedEffect(isFullScreen) {
        if (isFullScreen) {
            isUserPanning = false
        }
    }




    Log.i("OBSERVE", "current viewer: " + viewerInfo.toString())


    //    Column(
    //        modifier = Modifier.fillMaxWidth()
    //            .padding(top = 16.dp)
    //    ) {


    /*.shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.inversePrimary,
                spotColor = MaterialTheme.colorScheme.inversePrimary,
                clip = false
            )*/

    //        Card(
    //            modifier = Modifier.shadow(
    //                elevation = 24.dp,
    //                shape = RoundedCornerShape(16.dp),
    //                ambientColor = MaterialTheme.colorScheme.inversePrimary,
    //                spotColor = MaterialTheme.colorScheme.inversePrimary,
    //                clip = false
    //            ).fillMaxWidth()
    //                .height(220.dp)
    //        ) {

    val scope = rememberCoroutineScope()

    var selectedUser by remember {
        mutableStateOf<String?>(null)
    }



    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {

        GoogleMap(
            modifier = Modifier
                .matchParentSize()
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp)),
            cameraPositionState = cameraPositionState,
            onMapClick = {},
            onMapLongClick = {},
            uiSettings = uiSettings,
            properties = mapProperties,
            onMapLoaded = { mapLoaded = true }
        ) {
            locations.forEach { (userId, state) ->
                val markerState = markerStates.getOrPut(userId) {
                    MarkerState()
                }

                UserMarker(
                    userId = userId,
                    markerState = markerState,
                    state = state,
                    isSelected = selectedUser == userId
                )
            }

            Log.i("POLYLINE", "selected user: $selectedUser")

            userPaths.forEach { (userId, path) ->

                val state = locations[userId]

                if (path.points.size > 1 && state != null && selectedUser == userId) {

                    Log.i("POLYLINE", "size: ${path.points.size}")
                    val isEmergency =
                        state is ViewerLocationState.EmergencySharing

                    Polyline(
                        points = path.points.take(uiState.playbackIndex + 1),
                        color = when {
                            isEmergency ->
                                Color.Red

                            path.isActive ->
                                Color.Blue

                            else ->
                                Color.Gray // summary line
                        },
                        width = if (isEmergency) 10f else 6f,
                        jointType = JointType.ROUND,
                        startCap = RoundCap(),
                        endCap = RoundCap(),
                        zIndex = if (isEmergency) 1f else 0f
                    )
                }
            }

            // Live Stay Point Markers
            val liveStays by viewModel.liveStayPoints.collectAsState()

            liveStays.forEach { (_, stayPoints) ->
                stayPoints.forEach { stay ->
                    val stayPos = LatLng(stay.lat, stay.lng)

                    // Duration text from the pre-computed durationMillis
                    // Updates reactively as the StateFlow emits new snapshots
                    val mins = stay.durationMillis / 60_000
                    val durationText = if (mins < 60) "Stayed $mins min"
                    else "${mins / 60}h ${mins % 60}m"

                    Marker(
                        state = MarkerState(position = stayPos),
                        title = durationText,
                        snippet = "Stay Point",
                        anchor = Offset(0.5f, 0.5f),
                        icon = stayPointMarkerIcon()
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

//        LaunchedEffect(isFullScreen, mapLoaded) {
//            if (!mapLoaded) return@LaunchedEffect
//
//            cameraPositionState.move(
//                CameraUpdateFactory.newCameraPosition(initialCameraPosition)
//            )
//        }

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
            MapType.NORMAL -> "Normal"
            MapType.SATELLITE -> "Satellite"
            MapType.TERRAIN -> "Terrain"
            MapType.HYBRID -> "Hybrid"
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = manrope,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        AnimatedVisibility(isFullScreen && showTray) {
            ViewerTrayOverlay(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(vertical = 16.dp),
                uiState.lovedOnes,
                locations,
                onHideClick = {
                    selectedUser = null
                    onHideClick()
                },
                onUserClick = {
                    selectedUser = it
                }
            ) {
                scope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            it,
                            16f
                        )
                    )
                }
            }
        }

        if (isFullScreen) {
            val liveCount = locations.count {
                it.value is ViewerLocationState.NormalSharing ||
                        it.value is ViewerLocationState.EmergencySharing
            }

            HorizontalFloatingToolbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-36).dp)
                    .zIndex(1f),
                expanded = true,
                leadingContent = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isEmergencyActive)
                            MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = "$liveCount Live",
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = if (isEmergencyActive)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }


                    Spacer(Modifier.width(8.dp))
                },
                trailingContent = {
                    // Tray toggle
                    IconButton(
                        onClick = {
                            if (showTray) onHideClick() else onShowTray()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.tray),
                            contentDescription = "Toggle Tray",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    // Fit All markers
                    IconButton(
                        onClick = {
                            if (visibleLatLngs.isNotEmpty()) {
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
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.full_screen),
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
                            showMapModeToast = true
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
                },
                content = {
                    FilledIconButton(
                        modifier = Modifier
                            .size(48.dp),
                        onClick = {
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
                        colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isEmergencyActive)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary,
                            contentColor = if (isEmergencyActive)
                                MaterialTheme.colorScheme.onError
                            else
                                MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isEmergencyActive) R.drawable.emergency
                                else R.drawable.gps
                            ),
                            contentDescription = if (isEmergencyActive)
                                "Center on Emergency"
                            else
                                "Fit All Users",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
            )
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
                Log.i("POLYLINE", "select")
                scope.launch {
                    tooltipState.show()
                }
            }
        ) {

            // Avatar
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

            // Name
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


/*
Text(
                "Watching: $text",
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )*/

@Preview(showBackground = true)
@Composable
fun ViewerTrayOverlay(
    modifier: Modifier = Modifier,
    viewerList: List<CircleMember>,
    locations: Map<String, ViewerLocationState>,
    onHideClick: () -> Unit,
    onUserClick: (String) -> Unit,
    onClick: (LatLng) -> Unit
) {
    val filtered = viewerList.filter {
        it.id in locations.keys.toSet()
    }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright.copy(0.85f)
        ),
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                filtered.forEach { conn ->
                    RichTooltipExample(
                        conn = conn,
                        viewerState = locations[conn.id],
                        onClick = onClick,
                        onUserClick = onUserClick
                    )
                }
            }

            TextButton(
                onClick = onHideClick,
                modifier = Modifier
                    .padding(top = 68.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Text(
                    "Hide Tray",
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
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
//            .clickable {
//                navigateToObserve()
//            }
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
                    tint = MaterialTheme.colorScheme.primary,
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


fun createUserMarkerBitmap(
    isEmergency: Boolean
): Bitmap {
    val size = 96
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Emergency glow
    if (isEmergency) {
        paint.color = android.graphics.Color.RED

        paint.alpha = 60
        canvas.drawCircle(size / 2f, size / 2f, size / 2.2f, paint)
    }

    // Main dot
    paint.alpha = 255
    paint.color = if (isEmergency) android.graphics.Color.RED else android.graphics.Color.BLUE
    canvas.drawCircle(size / 2f, size / 2f, size / 4f, paint)

    // White border
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 4f
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 4f, paint)

    return bitmap
}

fun markerIcon(
    isEmergency: Boolean
): BitmapDescriptor {
    return BitmapDescriptorFactory.fromBitmap(
        createUserMarkerBitmap(isEmergency)
    )
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
fun animateLatLngAsState(
    target: LatLng
): State<LatLng> {

    val lat = remember(target.latitude) {
        Animatable(target.latitude.toFloat())
    }
    val lng = remember(target.longitude) {
        Animatable(target.longitude.toFloat())
    }

    LaunchedEffect(target) {
        coroutineScope {
            launch {
                lat.animateTo(
                    target.latitude.toFloat(),
                    tween(1000, easing = LinearOutSlowInEasing)
                )
            }
            launch {
                lng.animateTo(
                    target.longitude.toFloat(),
                    tween(1000, easing = LinearOutSlowInEasing)
                )
            }
        }
    }

    return remember {
        derivedStateOf {
            LatLng(lat.value.toDouble(), lng.value.toDouble())
        }
    }
}

@Composable
fun UserMarker(
    userId: String,
    markerState: MarkerState,
    state: ViewerLocationState,
    isSelected: Boolean
) {
    if (state is ViewerLocationState.Blocked) return


    val location = when (state) {
        is ViewerLocationState.NormalSharing -> state.location
        is ViewerLocationState.EmergencySharing -> state.location
        else -> return
    }

    val target = LatLng(location.lat, location.lng)

    // Only update automatically if not selected
    if (!isSelected) {
        LaunchedEffect(target) {
            markerState.position = target
        }
    }

    val isEmergency = state is ViewerLocationState.EmergencySharing

    Marker(
        state = markerState,
        icon = if (isEmergency) markerIcon(true) else markerIcon(false),
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

//fun formatTime(timestamp: Long): String {
//    val diff = System.currentTimeMillis() - timestamp
//
//    val seconds = diff / 1000
//    val minutes = seconds / 60
//    val hours = minutes / 60
//    val days = hours / 24
//
//    return when {
//        seconds < 60 -> "Just now"
//        minutes < 60 -> "$minutes min ago"
//        hours < 24 -> "$hours hr ago"
//        days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
//        else -> {
//            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
//            sdf.format(java.util.Date(timestamp))
//        }
//    }
//}