package com.aarav.geowav.presentation.addplace

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.model.UpgradeContext
import com.aarav.geowav.data.model.UpgradeEvents
import com.aarav.geowav.presentation.components.CustomBottomSheet
import com.aarav.geowav.presentation.components.CustomChip
import com.aarav.geowav.presentation.components.MyAlertDialog
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
    placeViewModel: PlaceViewModel,
    subscriptionVM: SubscriptionViewModel
) {

    var upgradeContext by remember { mutableStateOf<UpgradeContext?>(null) }


    val plan by subscriptionVM.userPlan.collectAsState()

    val uiState by placeViewModel.uiState.collectAsState()

    val selectedPlace = uiState.selectedPlace

    var placeName by remember {
        mutableStateOf(selectedPlace?.displayName ?: "")
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
                        "$placeName added to geofence",
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
            placeName = it
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
        title = "Unable To Fetch",
        message = uiState.error ?: "Unable to fetch place details",
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
                        text = "Add Place",
                        fontWeight = FontWeight.Normal,
                        fontFamily = manrope
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
                    FilledTonalButton(
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
                            text = "Save Place",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontFamily = manrope,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) {

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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.map_pin_area),
                                contentDescription = "map pin",
                                modifier = Modifier.padding(12.dp),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
                            )
                        }

                        selectedPlace?.let { place ->
                            Text(
                                text = place.displayName ?: "Place Name Unavailable",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                fontFamily = manrope,
                                fontWeight = FontWeight.Bold,
                            )

                            Text(
                                text = place.shortFormattedAddress ?: "Address Unavailable",
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = manrope,
                            )

                            Text(
                                text = "Lat: ${
                                    place.location?.latitude?.toString()?.take(7)
                                }, Lng: ${place.location?.longitude?.toString()?.take(7)}",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.W500,
                                fontFamily = manrope,
                            )

                        }

                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                PlaceTextField(
                    labelText = "Custom name",
                    placeHolder = "Enter name",
                    infoText = "Customize this place",
                    name = placeName,
                    onValueChange = { place ->
                        placeName = place
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                RadiusChipGroup(
                    chips = uiState.chips,
                    selectedRadius = uiState.selectedRadius
                ) { radius ->
                    placeViewModel.onRadiusChange(radius)

                    Log.i("MYTAG", "Selected Radius : ${uiState.selectedRadius}")
                }

                Spacer(modifier = Modifier.height(0.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Trigger Type : ",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = manrope,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    CustomChip("ENTRY")

                    CustomChip("EXIT")
                }


                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 16f)
                }

                LaunchedEffect(latlng) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latlng, 16f)
                }

                Spacer(modifier = Modifier.height(12.dp))


                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GoogleMap(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(16.dp)
                            )
                            .height(220.dp)
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
                                state = MarkerState(
                                    latlng,
//                            LatLng(
//                            selectedPlace?.location?.latitude ?: 0.0,
//                            selectedPlace?.location?.longitude ?: 0.0
//                        )
                                ),
                                title = selectedPlace?.displayName ?: ""
                            )
                        }

                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Preview",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontFamily = manrope,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontWeight = FontWeight.SemiBold
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