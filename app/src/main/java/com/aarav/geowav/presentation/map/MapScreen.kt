package com.aarav.geowav.presentation.map

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.presentation.components.MyAlertDialog
import com.aarav.geowav.presentation.components.PermissionRequiredContent
import com.aarav.geowav.presentation.components.PlaceModalSheet
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.surfaceContainerLowDarkHighContrast
import com.aarav.geowav.presentation.theme.surfaceContainerLowestLightHighContrast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MapScreen(
    isDarkThemeEnabled: Boolean,
    mapViewModel: MapViewModel,
    location: Pair<Double, Double>?,
    hasForegroundLocationPermission: Boolean,
    navigateToAddPlace: (String) -> Unit,
    navigateToManualAddPlace: (Double, Double) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {


    val uiState by mapViewModel.uiState.collectAsState()

    val cameraPositionState = rememberCameraPositionState()

    var mapProperties by remember {
        mutableStateOf(MapProperties(isMyLocationEnabled = hasForegroundLocationPermission))
    }

    LaunchedEffect(hasForegroundLocationPermission) {
        mapProperties = mapProperties.copy(isMyLocationEnabled = hasForegroundLocationPermission)
    }

    var mapLoaded by remember {
        mutableStateOf(false)
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

    val selectedPlace = uiState.selectedPlace
    val manualSelectedLatLng = uiState.manualSelectedLatLng

    location?.let { (lat, lng) ->
        LaunchedEffect(lat, lng) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(lat, lng), 16f)
        }
    }

    MyAlertDialog(
        shouldShowDialog = uiState.showErrorDialog,
        onDismissRequest = { mapViewModel.clearError() },
        title = "Error",
        message = uiState.error ?: "Unable to fetch place details",
        confirmButtonText = "Dismiss"
    ) {
        mapViewModel.clearError()
    }

    LaunchedEffect(location) {
        Log.i("MYTAG", "${location?.first} and ${location?.second}")
    }

    val placeSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    val textFieldState = rememberTextFieldState()

    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Text(
                        text = "Select Place",
                        fontFamily = manrope
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = navigateToHome
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = null
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if(hasForegroundLocationPermission && manualSelectedLatLng == null) {
                FloatingActionButton(
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = {
                        location?.let { (lat, lng) ->
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 16f)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.gps),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                        contentDescription = "My Location", modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        if (!hasForegroundLocationPermission) {
            PermissionRequiredContent(
                title = "Location access is needed",
                message = "Adding places uses your current area to help position the map and set up reliable place alerts.",
                primaryActionText = "Review setup",
                onPrimaryAction = navigateToSettings,
                secondaryActionText = "Go back",
                onSecondaryAction = navigateToHome,
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

//            SearchBar(
//                modifier = if(expanded) Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(0.dp) else
//                    Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(horizontal = 12.dp),
//                inputField = {
//                    SearchBarDefaults.InputField(
//                        query = textFieldState.text.toString(),
//                        onQueryChange = { textFieldState.edit { replace(0, length, it) } },
//                        onSearch = {
//
//                            expanded = false
//                        },
//                        expanded = expanded,
//                        onExpandedChange = { expanded = it },
//                        placeholder = { Text("Search") }
//                    )
//                },
//                expanded = expanded,
//                onExpandedChange = { expanded = it },
//            ) { }

            if(!mapLoaded) {
                ContainedLoadingIndicator(
                    Modifier.align(Alignment.Center)
                )
            }


            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                properties = mapProperties,
                uiSettings = uiSettings,
                onMapLoaded = {
                    mapLoaded = true
                },
                cameraPositionState = cameraPositionState,
                onMapClick = { /* Search remains the tap-first path for known places. */ },
                onMapLongClick = { latLng ->
                    mapViewModel.selectManualPlace(latLng)
                }
            ) {
                /* place.let {
                       Marker(
                           state = MarkerState(position = LatLng(it?.location?.latitude ?: 0.0, it?.location?.longitude ?: 0.0)
                           ),
                           title = place?.displayName
                       )
                   }*/

                if (uiState.selectedPlace != null) {
                    Marker(
                        state = MarkerState(
                            position = LatLng(
                                selectedPlace?.location?.latitude ?: 0.0,
                                selectedPlace?.location?.longitude ?: 0.0
                            )
                        ),
                        onClick = {
                            mapViewModel.showBottomSheet()
                            return@Marker true
                        },
                        title = selectedPlace?.displayName
                    )
                }

                manualSelectedLatLng?.let { latLng ->
                    Marker(
                        state = MarkerState(position = latLng),
                        title = "Dropped pin"
                    )
                }
            }

            LaunchedEffect(textFieldState.text) {
                if (textFieldState.text.length > 2 &&
                    !uiState.showErrorDialog
                ) {

                    delay(300) // debounce
                    mapViewModel.searchPlaces(textFieldState.text.toString())
                }
            }



            PlaceModalSheet(
                place = selectedPlace,
                sheetState = placeSheetState,
                showSheet = uiState.isBottomSheetShowing,
                onAddPlaceBtnClick = navigateToAddPlace,
                clearSearch = {
                    textFieldState.clearText()
                },
                onDismissRequest = {
                    mapViewModel.dismissBottomSheet()
                }
            )

            LaunchedEffect(selectedPlace) {
                selectedPlace?.let {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                it.location?.latitude ?: 0.0, it.location?.longitude ?: 0.0
                            ), 16f
                        )
                    )
                }
            }

            NewSearch(
                isDarkThemeEnabled,
                predictions = uiState.predictions,
                expanded = uiState.isSearchExpanded,
                modifier = Modifier.align(Alignment.TopCenter),
                onExpandedChange = {
                    mapViewModel.onExpandChange(it)
                },
                isLoading = uiState.isLoading,
                onQueryChange = { textFieldState.edit { replace(0, length, it) } },
                onPlaceSelected = { place ->
                    mapViewModel.fetchPlace(place)
                    textFieldState.clearText()
                },
                textFieldState = textFieldState
            )

            manualSelectedLatLng?.let { latLng ->
                ManualPlacePreview(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    onContinue = {
                        navigateToManualAddPlace(latLng.latitude, latLng.longitude)
                    }
                )
            }
            /*
             {
                location?.let { (lat, lng) ->
                    Marker(state = MarkerState(position = LatLng(lat, lng)), title = "You are here")
                }
            }
            */


        }

    }
}

@Composable
private fun ManualPlacePreview(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    painter = painterResource(R.drawable.map_pin_area),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Dropped pin",
                    fontFamily = manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "Approximate place",
                    fontFamily = manrope,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(12.dp))

            FilledTonalButton(
                onClick = onContinue,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Continue",
                    fontFamily = manrope,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
