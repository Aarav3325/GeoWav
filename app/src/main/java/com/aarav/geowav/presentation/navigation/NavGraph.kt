package com.aarav.geowav.presentation.navigation

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aarav.geowav.domain.authentication.GoogleSignInClient
import com.aarav.geowav.presentation.GeoAlert
import com.aarav.geowav.presentation.GeoConnection
import com.aarav.geowav.presentation.GeoWavHomeScreen
import com.aarav.geowav.presentation.GeoZone
import com.aarav.geowav.presentation.auth.LoginScreen
import com.aarav.geowav.presentation.auth.SignupScreen
import com.aarav.geowav.presentation.map.AddPlaceScreen
import com.aarav.geowav.presentation.map.MapScreen
import com.aarav.geowav.presentation.map.MapViewModel
import com.aarav.geowav.presentation.map.PlaceViewModel
import com.aarav.geowav.presentation.map.YourPlacesScreen
import com.aarav.geowav.presentation.onboard.OnboardingScreen

@Composable
fun NavGraph(
    navHostController: NavHostController,
    sharedPreferences: SharedPreferences,
    location: Pair<Double, Double>?,
    googleSignInClient: GoogleSignInClient,
    mapViewModel: MapViewModel,
    placesViewModel: PlaceViewModel,
    modifier: Modifier
) {
    val isLoggedIn = googleSignInClient.isLoggedIn()
    val isOnboarded = sharedPreferences.getBoolean("isOnboarded", false)

    NavHost(
        modifier = modifier,
        navController = navHostController,
        startDestination = if (isLoggedIn && isOnboarded) NavRoute.HomeScreen.path else if(!isOnboarded) NavRoute.OnBoard.path
        else if(!isLoggedIn && isOnboarded) NavRoute.Login.path else NavRoute.SignUp.path
    ) {
        AddMapsScreen(
            navHostController,
            this,
            location,
            mapViewModel,
            placesViewModel
        )

        AddNewPlaceScreen(
            navHostController,
            this,
            mapViewModel,
            placesViewModel
        )

        AddYourPlacesScreen(
            navHostController,
            this,
            placesViewModel
        )

        AddSignUpScreen(
            navHostController,
            this,
            googleSignInClient
        )

        AddLoginScreen(
            navHostController,
            this,
            googleSignInClient
        )

        AddOnBoard(
            navHostController,
            this,
            sharedPreferences
        )

        AddHomeScreen(
            navHostController,
            this,
            googleSignInClient
        )
    }

}

fun AddMapsScreen(
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
    location: Pair<Double, Double>?,
    mapViewModel: MapViewModel, placesViewModel: PlaceViewModel
) {
    navGraphBuilder.composable(
        route = NavRoute.MapScreen.path
    ) {
        MapScreen(
            mapViewModel,
            location,
            placesViewModel,
            navigateToAddPlace = { id ->
                navController.navigate(NavRoute.AddPlace.createRoute(id))
            },
            navigateToHome = {
                navController.navigateUp()
            }
        )
    }
}

fun AddNewPlaceScreen(
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
    mapViewModel: MapViewModel,
    placesViewModel: PlaceViewModel
) {
    navGraphBuilder.composable(
        route = NavRoute.AddPlace.path.plus("/{placeId}"),
        arguments = listOf(
            navArgument("placeId") {
                type = NavType.StringType
            }
        )
    ) {
        val placeId = it.arguments?.get("placeId").toString()

        AddPlaceScreen(
            placeId,
            navigateToMaps = {
                navController.navigateUp()
            },
            navigateToYourPlaces = {
                navController.navigate(NavRoute.YourPlaces.path)
            },
            mapViewModel,
            placesViewModel
        )

    }
}

fun AddYourPlacesScreen(
    navController: NavController, navGraphBuilder: NavGraphBuilder, placesViewModel: PlaceViewModel
) {
    navGraphBuilder.composable(
        route = NavRoute.YourPlaces.path
    ) {
        YourPlacesScreen(
            placesViewModel,
            navigateToMap = {
                navController.navigate(NavRoute.MapScreen.path)
            }
        )
    }
}

fun AddSignUpScreen(
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
    googleSignInClient: GoogleSignInClient
) {
    navGraphBuilder.composable(
        route = NavRoute.SignUp.path
    ) {
        SignupScreen(
            googleSignInClient,
            navigateToHome = {
                navController.navigate(NavRoute.HomeScreen.path)
            },
            navigateToLogin = {
                navController.navigate(NavRoute.Login.path)
            }
        )
    }
}

fun AddLoginScreen(
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
    googleSignInClient: GoogleSignInClient
) {
    navGraphBuilder.composable(
        route = NavRoute.Login.path
    ) {
        LoginScreen(
            googleSignInClient,
            navigateToMap = {
                navController.navigate(NavRoute.HomeScreen.path)
            },
            navigateToSignUp = {
                navController.navigate(NavRoute.SignUp.path)
            }
        )
    }
}

fun AddOnBoard(navController: NavController, navGraphBuilder: NavGraphBuilder,
               sharedPreferences: SharedPreferences) {
    navGraphBuilder.composable(
        route = NavRoute.OnBoard.path
    ) {
        OnboardingScreen(
            navigateToAuth = {
                navController.navigate(NavRoute.SignUp.path)
            },
            sharedPreferences
        )
    }
}

fun AddHomeScreen(
    navController: NavController, navGraphBuilder: NavGraphBuilder,
    googleSignInClient: GoogleSignInClient
) {
    navGraphBuilder.composable(
        route = NavRoute.HomeScreen.path
    ) {

        val sampleConnections = listOf(
            GeoConnection("1", "Anushree", true),
            GeoConnection("2", "Akshat", true),
            GeoConnection("3", "Mummy", false)
        )

        val sampleZones = listOf(
            GeoZone("z1", "Home", true, 200),
            GeoZone("z2", "Office", false, 300)
        )

        val sampleAlerts = listOf(
            GeoAlert(
                "a1",
                "Entered Home",
                "200m • Sending auto updates to your circle",
                "5:42 PM",
                "enter"
            ),
            GeoAlert("a2", "Left Office", "You left Office • 3 mins ago", "3:12 PM", "exit"),
            GeoAlert(
                "a2",
                "Entered Office",
                "300m • Sending auto updates to your circle",
                "9:15 AM",
                "enter"
            ),
            GeoAlert("a2", "Left Home", "10 mins ago", "8:46 AM", "exit")
        )

        GeoWavHomeScreen(
            navigateToAuth = {
                navController.navigate(NavRoute.Login.path) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            googleSignInClient,
            connections = sampleConnections,
            zones = sampleZones,
            alerts = sampleAlerts,
            onViewMap = {},
            onAddZone = {
                navController.navigate(NavRoute.MapScreen.path)
            },
            onShareLocation = {},
            onOpenAlerts = {}
        )
    }
}
