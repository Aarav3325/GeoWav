package com.aarav.geowav

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aarav.geowav.data.geofence.GeofenceForegroundService
import com.aarav.geowav.data.geofence.GeofencingVM
import com.aarav.geowav.domain.authentication.GoogleSignInClient
import com.aarav.geowav.presentation.components.SnackbarManager
import com.aarav.geowav.presentation.map.AddLocationFAB
import com.aarav.geowav.presentation.map.MapViewModel
import com.aarav.geowav.presentation.map.PlaceViewModel
import com.aarav.geowav.presentation.navigation.BottomNavigationBar
import com.aarav.geowav.presentation.navigation.NavGraph
import com.aarav.geowav.presentation.navigation.NavRoute
import com.aarav.geowav.ui.theme.GeoWavTheme
import com.aarav.geowav.ui.theme.manrope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.libraries.places.api.model.Place
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()



        setContent {

            val mapViewModel: MapViewModel = hiltViewModel()
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

            val geoVM: GeofencingVM = hiltViewModel()

            val placesViewModel: PlaceViewModel = hiltViewModel()


            val places by placesViewModel.allPlaces.collectAsState()




            val location by mapViewModel.currentLocation.collectAsState()

            mapViewModel.startLocationUpdates()


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

            var signOut by remember {
                mutableStateOf(false)
            }


            GeoWavTheme {

                val fineLocationPermission =
                    rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
                val backgroundLocationPermission =
                    rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

                val permissionsGranted =
                    fineLocationPermission.status.isGranted && backgroundLocationPermission.status.isGranted

//                val fineLocationPermission = rememberMultiplePermissionsState(
//                    permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION)
//                )
//                val backgroundPermission = rememberMultiplePermissionsState(
//                    permissions = listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//                )

                if (permissionsGranted) {
                    val intent = Intent(context, GeofenceForegroundService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                }


//                LaunchedEffect(places) {
//                    if (permissionsGranted) {
//                        geoVM.registerPlaces(places)
//                    }
//                }

                val navController = rememberNavController()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomRoutes = listOf(
                    NavRoute.HomeScreen.path,
                    NavRoute.ActivityScreen.path,
                    NavRoute.YourPlaces.path
                )

                val isBottomBarVisible = currentRoute in showBottomRoutes


                val snackbarHostState = remember {
                    SnackbarHostState()
                }

                LaunchedEffect(snackbarHostState) {
                    SnackbarManager.bind(snackbarHostState)
                }
//                LaunchedEffect(signOut) {
//                    delay(800)
//                    signOut = true
//                    googleSignInClient.firebaseSignOut()
//                    navController.navigate(NavRoute.SignUp.path)
//                }

//                val showTop = listOf()

                Scaffold(
//                    topBar = {
//                        if (currentRoute.equals(NavRoute.YourPlaces.path)) {
//                            TopAppBar(
//                                title = {
//                                    Text(
//                                        text = "Your Places",
//                                        fontSize = 24.sp,
//                                        fontFamily = manrope,
//                                        fontWeight = FontWeight.SemiBold,
//                                        color = MaterialTheme.colorScheme.onBackground
//                                    )
//                                },
//                                navigationIcon = {
////                    IconButton(
////                        onClick = {
////                            navigateToMap()
////                        }
////                    ) {
////                        Icon(
////                            imageVector = Icons.Default.ArrowBack,
////                            contentDescription = null
////                        )
////                    }
//                                }
//                            )
//                        }
//                    },
//                        floatingActionButton = {
//                            AddLocationFAB()
//                        },
                    snackbarHost = {
                        //SnackbarManager.bind(snackbarHostState)
                        SnackbarHost(snackbarHostState)
                    },
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0),
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    bottomBar = {
                        AnimatedVisibility(isBottomBarVisible) {
                            BottomNavigationBar(navController)
                        }
                    }
                ) {
                    val location1 =
                        location?.let { it.latitude to it.longitude } ?: (0.0 to 0.0)


                    NavGraph(
                        navController,
                        sharedPreferences,
                        location1,
                        googleSignInClient,
                        mapViewModel,
                        placesViewModel,
                        modifier = Modifier.padding(it)
                    )
                }
//                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    when {
////                        !fineLocationPermission.allPermissionsGranted -> {
////                            Button(onClick = { fineLocationPermission.launchMultiplePermissionRequest() }) {
////                                Text("Grant foreground location")
////                            }
////                        }
////
////                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
////                                !backgroundPermission.allPermissionsGranted -> {
////                            Button(onClick = { backgroundPermission.launchMultiplePermissionRequest() }) {
////                                Text("Grant background location")
////                            }
////                        }
//
//
//                        else -> {







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
                //                    }
                //                  }
//

                //         }


//            Log.i("MYTAG", places.get(0).toString())
//                LaunchedEffect(places) {
//                    geoVM.registerPlaces(places)
//                }
            }
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
