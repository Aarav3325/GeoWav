package com.aarav.geowav

import NavGraph
import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aarav.geowav.data.geofence.GeofenceForegroundService
import com.aarav.geowav.domain.authentication.GoogleSignInClient
import com.aarav.geowav.presentation.components.SnackbarManager
import com.aarav.geowav.presentation.map.MapViewModel
import com.aarav.geowav.presentation.navigation.BottomNavigationBar
import com.aarav.geowav.presentation.navigation.NavRoute
import com.aarav.geowav.presentation.place.PlaceViewModel
import com.aarav.geowav.ui.theme.GeoWavTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
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
            val placesViewModel: PlaceViewModel = hiltViewModel()


            val location by mapViewModel.currentLocation.collectAsState()

            mapViewModel.startLocationUpdates()


            val context = LocalContext.current

            GeoWavTheme {

                val fineLocationPermission =
                    rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
                val backgroundLocationPermission =
                    rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

                val permissionsGranted =
                    fineLocationPermission.status.isGranted && backgroundLocationPermission.status.isGranted



                if (permissionsGranted) {
                    val intent = Intent(context, GeofenceForegroundService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                }


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


                Scaffold(
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
            }
        }
    }
}