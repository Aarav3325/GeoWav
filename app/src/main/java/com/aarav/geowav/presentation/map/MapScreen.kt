package com.aarav.geowav.presentation.map

import android.location.Location
import android.util.Log
import com.aarav.geowav.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.aarav.geowav.presentation.components.PlaceModalSheet
import com.aarav.geowav.ui.theme.sora
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(mapViewModel: MapViewModel,
              location : Pair<Double, Double>?,
              placesViewModel : PlaceViewModel,
              navigateToAddPlace: (String) -> Unit,
              navigateToHome: () -> Unit,
              modifier: Modifier = Modifier) {


    val cameraPositionState = rememberCameraPositionState()

    var mapProperties by remember {
        mutableStateOf(MapProperties(isMyLocationEnabled = true))
    }

    var uiSettings by remember {
        mutableStateOf(MapUiSettings(myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
            compassEnabled = true))
    }


    location?.let { (lat, lng) ->
        LaunchedEffect(lat, lng) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(lat, lng), 16f)
        }
    }


    var showMarker by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(location) {
        Log.i("MYTAG", "${location?.first} and ${location?.second}")
    }


//        LaunchedEffect(place) {
//                cameraPositionState.animate(
//                    CameraUpdateFactory.newLatLngZoom(LatLng(place?.location?.latitude ?: 0.0, place?.location?.latitude ?: 0.0), 16f)
//                )
////            delay(2000)
////            onPlaceSelected(true)
//    }

    var showPlaceModalSheet by remember {
        mutableStateOf(false)
    }

    var placeSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    val textFieldState = rememberTextFieldState()

    var expanded by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    var selectedPlace by remember {
        mutableStateOf<Place?>(null)
    }


    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                title = {
                    Text(
                        text = "Select Place",
                        fontFamily = sora
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = navigateToHome
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    location?.let { (lat, lng) ->
                        scope.launch {
//                            mapViewModel.fetchUserLocation()
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
    ) {
            innerPadding ->
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


            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                properties = mapProperties,
                uiSettings = uiSettings,
                cameraPositionState = cameraPositionState,
                onMapClick = { /* later we'll handle adding a place */ }
            ) {
                /* place.let {
                       Marker(
                           state = MarkerState(position = LatLng(it?.location?.latitude ?: 0.0, it?.location?.longitude ?: 0.0)
                           ),
                           title = place?.displayName
                       )
                   }*/

                if (selectedPlace != null) {
                    Marker(
                        state = MarkerState(
                            position = LatLng(
                                selectedPlace?.location?.latitude ?: 0.0,
                                selectedPlace?.location?.longitude ?: 0.0
                            )
                        ),
                        onClick = {
                            showPlaceModalSheet = true
                            return@Marker true
                        },
                        title = selectedPlace?.displayName
                    )
                }
            }





            PlaceModalSheet(
                place = selectedPlace,
                sheetState = placeSheetState,
                showSheet = showPlaceModalSheet,
                onAddPlaceBtnClick = navigateToAddPlace,
                clearSearch = {
                    textFieldState.clearText()
                },
                onDismissRequest = {
                    showPlaceModalSheet = false
                }
            )

            NewSearch(
                context = context,
                expanded = expanded,
                modifier = Modifier.align(Alignment.TopCenter),
                onExpandedChange = {
                    expanded = it
                },
                onPlaceSelected = { place ->
                    selectedPlace = place
                    scope.launch {
                        selectedPlace?.let {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        it.location?.latitude ?: 0.0, it.location?.longitude ?: 0.0
                                    ), 16f
                                )
                            )
                        }

                        delay(1000)
                    }
                    showPlaceModalSheet = true
                    Log.d(
                        "PLACE",
                        "Selected: ${place.displayName} at ${place.location}"
                    )
                },
                placeViewModel = placesViewModel,
                textFieldState = textFieldState
            )
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
