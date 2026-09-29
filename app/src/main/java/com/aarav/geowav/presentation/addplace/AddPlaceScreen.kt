package com.aarav.geowav.presentation.addplace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.model.UpgradeContext
import com.aarav.geowav.data.model.UpgradeEvents
import com.aarav.geowav.presentation.components.CustomBottomSheet
import com.aarav.geowav.presentation.components.MyAlertDialog
import com.aarav.geowav.presentation.components.PermissionRequiredContent
import com.aarav.geowav.presentation.components.SnackbarManager
import com.aarav.geowav.presentation.components.UpgradeBottomSheetContent
import com.aarav.geowav.presentation.components.openAppDetailsSettings
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.manrope
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true)
@Composable
fun AddPlaceScreen(
    isDarkThemeEnabled: Boolean,
    placeId: String?,
    manualLatLng: LatLng? = null,
    manualAddress: String? = null,
    navigateToMaps: () -> Unit,
    navigateToPaywall: () -> Unit,
    navigateToYourPlaces: () -> Unit,
    navigateToSettings: () -> Unit,
    locationServicesReady: Boolean,
    placeViewModel: PlaceViewModel,
    subscriptionVM: SubscriptionViewModel
) {
    var upgradeContext by remember { mutableStateOf<UpgradeContext?>(null) }
    val plan by subscriptionVM.userPlan.collectAsState()
    val uiState by placeViewModel.uiState.collectAsState()

    val selectedPlace = uiState.selectedPlace
    val isManualPlace = manualLatLng != null
    val placeTitle = selectedPlace?.displayName ?: if (isManualPlace) "Dropped pin" else "New Place"
    val placeAddress = selectedPlace?.shortFormattedAddress
        ?: if (isManualPlace) manualAddress ?: "Approximate location" else "Address Unavailable"

    var placeName by remember {
        mutableStateOf(if (isManualPlace) "Dropped pin" else extractShortPlaceName(selectedPlace?.displayName))
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        placeViewModel.events.collect { event ->
            if (event is UpgradeEvents.ShowUpgrade) {
                upgradeContext = event.upgradeContext
            }
        }
    }

    LaunchedEffect(Unit) {
        placeViewModel.placeEvents.collect { event ->
            when (event) {
                is PlacesEvent.Error -> {
                    SnackbarManager.showMessage(event.message)
                }
                is PlacesEvent.Success -> {
                    navigateToYourPlaces()
                }
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

    LaunchedEffect(selectedPlace) {
        selectedPlace?.displayName?.let {
            placeName = extractShortPlaceName(it)
        }
    }

    var latlng by remember {
        mutableStateOf(manualLatLng ?: LatLng(0.0, 0.0))
    }

    LaunchedEffect(selectedPlace) {
        selectedPlace?.location?.let {
            latlng = LatLng(it.latitude, it.longitude)
        }
    }

    LaunchedEffect(manualLatLng) {
        manualLatLng?.let {
            latlng = it
        }
    }

    LaunchedEffect(placeId, isManualPlace) {
        if (!isManualPlace && !placeId.isNullOrBlank()) {
            placeViewModel.fetchPlace(placeId)
        }
    }

    MyAlertDialog(
        shouldShowDialog = uiState.showErrorDialog,
        onDismissRequest = {
            placeViewModel.clearError()
        },
        title = "Couldn't load this place",
        message = uiState.error ?: "Check your connection and try again",
        confirmButtonText = "Dismiss"
    ) {
        placeViewModel.clearError()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Add Place",
                            fontWeight = FontWeight.Bold,
                            fontFamily = manrope,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = placeTitle,
                            fontWeight = FontWeight.Normal,
                            fontFamily = manrope,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = navigateToMaps
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = "Navigate back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        bottomBar = {
            if (locationServicesReady) {
                val buttonInteraction = remember { MutableInteractionSource() }
                val isPressed by buttonInteraction.collectIsPressedAsState()
                val buttonScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.98f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "saveButtonScale"
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 6.dp,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = {
                                val finalPlaceId = if (isManualPlace) {
                                    createManualPlaceId(latlng)
                                } else {
                                    placeId.orEmpty()
                                }

                                val finalPlace = Place(
                                    placeId = finalPlaceId,
                                    customName = placeName.trim().ifBlank { placeTitle },
                                    placeName = placeTitle,
                                    latitude = latlng.latitude,
                                    longitude = latlng.longitude,
                                    address = placeAddress,
                                    radius = uiState.selectedRadius,
                                    triggerType = "ENTER_EXIT",
                                    addedOn = getFormattedDate()
                                )

                                placeViewModel.addPlace(finalPlace, plan)
                            },
                            interactionSource = buttonInteraction,
                            enabled = placeName.isNotBlank() || placeTitle.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .graphicsLayer(scaleX = buttonScale, scaleY = buttonScale),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.check),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Add to My Places",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = manrope,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->

        if (!locationServicesReady) {
            PermissionRequiredContent(
                title = "Place alerts need location setup",
                message = "GeoWav needs live and background location access before it can save places that trigger entry and exit alerts.",
                primaryActionText = "Review setup",
                onPrimaryAction = { openAppDetailsSettings(context) },
                secondaryActionText = "Go back",
                onSecondaryAction = navigateToMaps,
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                ContainedLoadingIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Selected Place Overview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(R.drawable.map_pin_area),
                                    contentDescription = "Place icon",
                                    modifier = Modifier.size(22.dp),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = placeTitle,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontFamily = manrope,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = placeAddress,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = manrope
                            )
                        }
                    }
                }

                // 2. Custom Name Input
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = "Place Name",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = manrope
                        )
                    }

                    Text(
                        text = "Used in arrival and leaving notifications for your circle.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontFamily = manrope
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = placeName,
                        onValueChange = { placeName = it },
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "e.g. Home, Work, Gym, College",
                                fontFamily = manrope,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.map_pin),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            AnimatedVisibility(placeName.isNotEmpty()) {
                                IconButton(
                                    onClick = { placeName = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.close),
                                        contentDescription = "Clear name",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // 3. Awareness Radius Selection
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = "Awareness Radius",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = manrope
                        )
                    }

                    Text(
                        text = "Boundary distance that triggers arrival and departure updates.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontFamily = manrope
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        uiState.chips.forEach { radius ->
                            val isSelected = uiState.selectedRadius == radius
                            val chipBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer
                            val chipContentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            val chipBorder = if (isSelected) {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = chipBg,
                                border = chipBorder,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        placeViewModel.onRadiusChange(radius)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            painter = painterResource(R.drawable.check),
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = chipContentColor
                                        )
                                    }
                                    Text(
                                        text = "${radius.roundToInt()} m",
                                        fontFamily = manrope,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = chipContentColor
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.map_trifold),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Alerts trigger when someone moves within ${uiState.selectedRadius.roundToInt()}m of this center.",
                                fontFamily = manrope,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 4. Interactive Geofence Map Card
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 16f)
                }

                LaunchedEffect(latlng) {
                    if (latlng.latitude != 0.0 && latlng.longitude != 0.0) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latlng, 16f)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        GoogleMap(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp)),
                            cameraPositionState = cameraPositionState,
                            uiSettings = MapUiSettings(
                                compassEnabled = false,
                                indoorLevelPickerEnabled = false,
                                mapToolbarEnabled = false,
                                myLocationButtonEnabled = false,
                                rotationGesturesEnabled = false,
                                scrollGesturesEnabled = false,
                                scrollGesturesEnabledDuringRotateOrZoom = false,
                                tiltGesturesEnabled = false,
                                zoomControlsEnabled = false,
                                zoomGesturesEnabled = false
                            )
                        ) {
                            if (latlng.latitude != 0.0 && latlng.longitude != 0.0) {
                                Marker(
                                    state = rememberMarkerState(key = latlng.toString(), position = latlng),
                                    title = placeTitle
                                )

                                Circle(
                                    center = latlng,
                                    radius = uiState.selectedRadius.toDouble(),
                                    fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                    strokeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                                    strokeWidth = 2f
                                )
                            }
                        }

                        // Live Geofence Preview Badge
                        val infiniteTransition = rememberInfiniteTransition(label = "geofenceBadge")
                        val ringAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 0.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = LinearOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "geofenceRingAlpha"
                        )
                        val ringScale by infiniteTransition.animateFloat(
                            initialValue = 1.0f,
                            targetValue = 1.4f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = LinearOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "geofenceRingScale"
                        )

                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp),
                            shape = RoundedCornerShape(99.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .graphicsLayer(scaleX = ringScale, scaleY = ringScale)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = ringAlpha))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.tertiary)
                                    )
                                }
                                Text(
                                    text = "Geofence Preview · ${uiState.selectedRadius.roundToInt()}m",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = manrope,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

fun getFormattedDate(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date())
}

fun extractShortPlaceName(displayName: String?): String {
    if (displayName.isNullOrBlank()) return ""
    val delimiters = listOf(" - ", ", ", " (")
    val firstIndex = delimiters
        .mapNotNull { delimiter -> displayName.indexOf(delimiter).takeIf { it >= 0 } }
        .minOrNull()
    return if (firstIndex != null) displayName.substring(0, firstIndex).trim()
    else displayName.trim()
}

private fun createManualPlaceId(latLng: LatLng): String {
    val lat = "%.6f".format(Locale.US, latLng.latitude)
    val lng = "%.6f".format(Locale.US, latLng.longitude)
    return "manual_${lat}_${lng}_${System.currentTimeMillis()}"
}
