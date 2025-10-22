package com.aarav.geowav

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.aarav.geowav.data.geofence.GeofenceForegroundService
import com.aarav.geowav.data.geofence.GeofenceHelper
import com.aarav.geowav.data.geofence.GeofencingVM
import com.aarav.geowav.data.room.PlaceDatabase
import com.aarav.geowav.domain.place.PlaceRepositoryImpl
import com.aarav.geowav.presentation.components.PlaceModalSheet
import com.aarav.geowav.presentation.map.MapScreen
import com.aarav.geowav.presentation.map.MapViewModel
import com.aarav.geowav.presentation.map.NewSearch
import com.aarav.geowav.presentation.map.PlaceVMProvider
import com.aarav.geowav.presentation.map.PlaceViewModel
import com.aarav.geowav.presentation.navigation.BottomNavigationBar
import com.aarav.geowav.presentation.navigation.NavGraph
import com.aarav.geowav.ui.theme.GeoWavTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.MapView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {



    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()



        setContent {

            val mapViewModel : MapViewModel = hiltViewModel()
            val textFieldState = rememberTextFieldState()

            var expanded by remember {
                mutableStateOf(false)
            }

            var selectedPlace by remember {
                mutableStateOf<Place?>(null)
            }

            var showPlaceModalSheet by remember {
                mutableStateOf(false)
            }

            var placeSheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = false
            )

            val geoVM : GeofencingVM = hiltViewModel()

            val placesViewModel: PlaceViewModel = hiltViewModel()


            val places by placesViewModel.allPlaces.collectAsState()


            val location by mapViewModel.currentLocation.collectAsState()

            mapViewModel.startLocationUpdates()

            val serviceIntent = Intent(this, GeofenceForegroundService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)

            val context = LocalContext.current

//            LaunchedEffect(Unit) {
//                placesViewModel.addPlace(Place(
//        id = "School1",
//        name = "School",
//        lat = 22.476364,
//        lng = 70.056217,
//        radius = 200f
//    ))
//            }


            GeoWavTheme {
                val fineLocationPermission = rememberMultiplePermissionsState(
                    permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION)
                )
                val backgroundPermission = rememberMultiplePermissionsState(
                    permissions = listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                )

//                LaunchedEffect(places) {
//                    if (fineLocationPermission.allPermissionsGranted &&
//                        (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || backgroundPermission.allPermissionsGranted)
//                    ) {
//                        geoVM.registerPlaces(places)
//                    }
//                }

                val navController = rememberNavController()

//                Scaffold(
//                    modifier = Modifier.fillMaxSize(),
//                    containerColor = Color.Transparent,
////                    topBar = {
//////                        NewSearch(
//////                            context = context,
//////                            expanded = expanded,
//////                            onExpandedChange = {
//////                                expanded = it
//////                            },
//////                            onPlaceSelected = { place ->
//////                                selectedPlace = place
//////                                //showPlaceModalSheet = true
//////                                Log.d(
//////                                    "PLACE",
//////                                    "Selected: ${place.displayName} at ${place.location}"
//////                                )
//////                            },
//////                            placeViewModel = placesViewModel,
//////                            textFieldState = textFieldState
//////                        )
////                    },
//                    bottomBar = {
//                        BottomNavigationBar(navController)
//                    }
//                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        when {
                            !fineLocationPermission.allPermissionsGranted -> {
                                Button(onClick = { fineLocationPermission.launchMultiplePermissionRequest() }) {
                                    Text("Grant foreground location")
                                }
                            }

                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                                    !backgroundPermission.allPermissionsGranted -> {
                                Button(onClick = { backgroundPermission.launchMultiplePermissionRequest() }) {
                                    Text("Grant background location")
                                }
                            }


                            else -> {

                                val location1 = location?.let { it.latitude to it.longitude } ?: (0.0 to 0.0)


                                NavGraph(
                                    navController,
                                    location1,
                                    mapViewModel,
                                    placesViewModel
                                )

//                                PlaceModalSheet(
//                                    place = selectedPlace,
//                                    sheetState = placeSheetState,
//                                    showSheet = showPlaceModalSheet,
//                                ) {
//                                    showPlaceModalSheet = false
//                                }

//                                NewSearch(
//                                    context = context,
//                                    expanded = expanded,
//                                    modifier = Modifier.align(Alignment.TopCenter),
//                                    onExpandedChange = {
//                                        expanded = it
//                                    },
//                                    onPlaceSelected = { place ->
//                                        Log.d(
//                                            "PLACE",
//                                            "Selected: ${place.displayName} at ${place.location}"
//                                        )
//                                    },
//                                    placeViewModel = placesViewModel,
//                                    textFieldState = textFieldState
//                                )
                            }
                        }

                    }
                }



//            Log.i("MYTAG", places.get(0).toString())
//                LaunchedEffect(places) {
//                    geoVM.registerPlaces(places)
//                }
            }

        }
   // }
}

//@RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//fun testGeoWavFlow(context: Context, placesViewModel: PlaceViewModel) {
//    val geofenceHelper = GeofenceHelper(context)
//
//    val testPlace = Place(
//        id = "Home1",
//        name = "Home",
//        lat = 22.466560,
//        lng = 70.041085,
//        radius = 200f
//    )
//
//    // Save in Firebase
//    placesViewModel.addPlace(testPlace)
//
//    // Register geofence
//    geofenceHelper.addGeofence(testPlace,
//        onSuccess = { Log.d("MYTAG", "Geofence added for ${testPlace.name}") },
//        onError = { Log.e("MYTAG", "Failed to add geofence", it) }
//    )
//}
