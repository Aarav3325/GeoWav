package com.aarav.geowav.presentation.addplace

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
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
import com.aarav.geowav.presentation.components.PlaceTextField
import com.aarav.geowav.presentation.components.RadiusChipGroup
import com.aarav.geowav.presentation.components.SnackbarManager
import com.aarav.geowav.presentation.components.UpgradeBottomSheetContent
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.manrope
import com.google.android.gms.maps.model.CameraPosition
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
@Preview(showBackground = true)
@Composable
fun AddPlaceScreen(
    isDarkThemeEnabled: Boolean,
    placeId: String,
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

    var placeName by remember {
        mutableStateOf(extractShortPlaceName(selectedPlace?.displayName))
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
                    Toast.makeText(
                        context,
                        "$placeName added to your places",
                        Toast.LENGTH_LONG
                    ).show()
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
        mutableStateOf<LatLng>(LatLng(0.0, 0.0))
    }

    LaunchedEffect(selectedPlace) {
        selectedPlace?.location?.let {
            latlng = LatLng(it.latitude, it.longitude)
        }
    }


    LaunchedEffect(placeId) {
        placeViewModel.fetchPlace(placeId)
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedPlace?.displayName ?: "New Place",
                        fontWeight = FontWeight.Normal,
                        fontFamily = manrope,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = navigateToMaps,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = "back arrow"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (locationServicesReady) {
                Card(
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .graphicsLayer {
                            shadowElevation = 16f
                            shape = RoundedCornerShape(0.dp)
//                        clip = true
                            ambientShadowColor = Color.White.copy(alpha = 0.25f)
                            spotShadowColor = Color.White.copy(alpha = 0.25f)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(
                            start = 12.dp,
                            end = 12.dp,
                            top = 12.dp,
                            bottom = 28.dp
                        )
                    ) {
                        Button(
                            onClick = {

                                val finalPlace = Place(
                                    placeId = placeId,
                                    customName = placeName,
                                    placeName = selectedPlace?.displayName
                                        ?: "Place Name Unavailable",
                                    latitude = latlng.latitude,
                                    longitude = latlng.longitude,
                                    address = selectedPlace?.shortFormattedAddress
                                        ?: "Address Unavailable",
                                    radius = uiState.selectedRadius,
                                    triggerType = "ENTER_EXIT",
                                    addedOn = getFormattedDate()
                                )

                                placeViewModel.addPlace(finalPlace, plan)



                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Add to My Places",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontFamily = manrope,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    ) {

        if (!locationServicesReady) {
            PermissionRequiredContent(
                title = "Place alerts need location setup",
                message = "GeoWav needs live and background location access before it can save places that trigger entry and exit alerts.",
                primaryActionText = "Review setup",
                onPrimaryAction = navigateToSettings,
                secondaryActionText = "Go back",
                onSecondaryAction = navigateToMaps,
                modifier = Modifier.padding(it)
            )
            return@Scaffold
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ContainedLoadingIndicator()
            }
        }


        if (!uiState.isLoading) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
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
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.map_pin_area),
                                contentDescription = "map pin",
                                modifier = Modifier.padding(8.dp),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
                            )
                        }

                        selectedPlace?.let { place ->
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = place.displayName ?: "Place Name Unavailable",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontFamily = manrope,
                                    fontWeight = FontWeight.SemiBold,
                                )

                                Text(
                                    text = place.shortFormattedAddress ?: "Address Unavailable",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = manrope,
                                )
                            }
                        }
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))

                PlaceTextField(
                    labelText = "What do you call this place?",
                    placeHolder = "e.g. Home, Office, Rohan's House",
                    infoText = "Used in arrival and leaving alerts",
                    name = placeName,
                    onValueChange = { place ->
                        placeName = place
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Awareness radius",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope,
                    )
                    Text(
                        text = "How far from this place triggers an alert",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = manrope,
                    )
                    RadiusChipGroup(
                        chips = uiState.chips,
                        selectedRadius = uiState.selectedRadius
                    ) { radius ->
                        placeViewModel.onRadiusChange(radius)
                    }
                }


                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 16f)
                }

                LaunchedEffect(latlng) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latlng, 16f)
                }

                Spacer(modifier = Modifier.height(16.dp))


                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GoogleMap(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                            .clip(RoundedCornerShape(16.dp)),
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
                                state = MarkerState(latlng),
                                title = selectedPlace?.displayName ?: ""
                            )

                            com.google.maps.android.compose.Circle(
                                center = latlng,
                                radius = uiState.selectedRadius.toDouble(),
                                fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                strokeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                strokeWidth = 1.5f
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = "Awareness zone",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontFamily = manrope,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
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
