package com.aarav.geowav.presentation.home

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aarav.geowav.core.utils.ViewerLocationState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


@Preview(showBackground = true)
@Composable
fun ObserveLiveLocationCard(
    locations: Map<String, ViewerLocationState>
) {


    val cameraPositionState = rememberCameraPositionState()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .height(220.dp)
    ) {


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


        var userMovedCamera by remember { mutableStateOf(false) }


        // Find if any user is in emergency state
        val emergencyUser = locations
            .entries
            .firstOrNull { it.value is ViewerLocationState.EmergencySharing }

        var mapLoaded by remember { mutableStateOf(false) }

        /* Calculate a rectangle that contains all currently visible users, then smoothly
         move and zoom the map camera so everyone fits nicely inside the card*/
        // Normal mode camera behavior - show all visible users on map
        LaunchedEffect(visibleLatLngs, mapLoaded, emergencyUser) {
            if (!mapLoaded) return@LaunchedEffect // no camera animation until map loads
            if (visibleLatLngs.isEmpty()) return@LaunchedEffect // nothing to show on map

            if (emergencyUser != null) return@LaunchedEffect // no camera animation while user is in emergency state, override this later


            // Draws an invisible rectangle on map to show all visible users (zoom so that all users are visible)
            val bounds = LatLngBounds.builder().apply {
                visibleLatLngs.forEach { include(it) } // use include to add latlng to bounds
            }.build()
            // The smallest map rectangle that contains all visible users

            /* Move the camera so that this bounds fits completely inside the screen,
               Camera auto-zooms until all dots are visible. */
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(bounds, 80)
            )
        }


        /* “When an emergency is active, automatically focus the camera on the emergency user
            unless the user manually moved the map
         */
        LaunchedEffect(emergencyUser, mapLoaded) {
            if (!mapLoaded) return@LaunchedEffect // no animations before map is loaded
            if (emergencyUser == null) {
                userMovedCamera = false
                return@LaunchedEffect
            } /*
                if no emergency is active then normal camera logic will handle things
            */

            if (userMovedCamera) return@LaunchedEffect // if user manually drags then stop auto-follow

            val state = emergencyUser.value as ViewerLocationState.EmergencySharing
            val loc = state.location

            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(loc.lat, loc.lng),
                    16f
                )
            )
        }


        // Detect when user manually moves the map
        LaunchedEffect(cameraPositionState) {
            snapshotFlow {
                cameraPositionState.isMoving to
                        cameraPositionState.cameraMoveStartedReason
            }.collect { (isMoving, reason) ->
                // Detect user gesture specifically
                if (isMoving && reason == CameraMoveStartedReason.GESTURE) {
                    userMovedCamera = true
                }
            }
        }

        var uiSettings by remember {
            mutableStateOf(
                MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false,
                    compassEnabled = true
                )
            )
        }


        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp)),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            onMapLoaded = {
                mapLoaded = true
            }
        ) {
            locations.forEach { (userId, state) ->
                UserMarker(
                    userId = userId,
                    state = state
                )
            }

            emergencyUser?.let { (_, state) ->
                val emergencyState = state as ViewerLocationState.EmergencySharing
                val loc = emergencyState.location

                EmergencyRipple(
                    center = LatLng(loc.lat, loc.lng)
                )
            }
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
    state: ViewerLocationState
) {
    if (state is ViewerLocationState.Blocked) return

    val location = when (state) {
        is ViewerLocationState.NormalSharing -> state.location
        is ViewerLocationState.EmergencySharing -> state.location
        else -> return
    }

    val animatedPosition by animateLatLngAsState(
        LatLng(location.lat, location.lng)
    )

    val isEmergency = state is ViewerLocationState.EmergencySharing

//    if (isEmergency) {
//        EmergencyRipple(center = animatedPosition)
//    }

    val normalIcon = remember { markerIcon(false) }
    val emergencyIcon = remember { markerIcon(true) }

    val icon = if (isEmergency) emergencyIcon else normalIcon


    Marker(
        state = MarkerState(animatedPosition),
        icon = icon,
        anchor = Offset(0.5f, 0.5f),
        zIndex = if (isEmergency) 2f else 1f
    )
}
