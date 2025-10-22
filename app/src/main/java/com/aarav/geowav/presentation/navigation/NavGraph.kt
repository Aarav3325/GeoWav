package com.aarav.geowav.presentation.navigation

import android.location.Location
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aarav.geowav.domain.authentication.GoogleSignInClient
import com.aarav.geowav.presentation.auth.LoginScreen
import com.aarav.geowav.presentation.auth.SignupScreen
import com.aarav.geowav.presentation.map.AddPlaceScreen
import com.aarav.geowav.presentation.map.MapScreen
import com.aarav.geowav.presentation.map.MapViewModel
import com.aarav.geowav.presentation.map.PlaceViewModel
import com.aarav.geowav.presentation.map.YourPlacesScreen
import com.google.android.gms.maps.MapView

@Composable
fun NavGraph(navHostController: NavHostController,
             location: Pair<Double, Double>?,
             googleSignInClient: GoogleSignInClient,
             mapViewModel: MapViewModel,
             placesViewModel: PlaceViewModel) {

    NavHost(navController = navHostController,
        startDestination = if (googleSignInClient.isLoggedIn()) NavRoute.MapScreen.path else NavRoute.SignUp.path
    ){
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
    }

}

fun AddMapsScreen(navController: NavController,
                  navGraphBuilder: NavGraphBuilder,
                  location : Pair<Double, Double>?,
                  mapViewModel: MapViewModel, placesViewModel: PlaceViewModel
){
    navGraphBuilder.composable(
        route = NavRoute.MapScreen.path
    ){
        MapScreen(
            mapViewModel,
            location,
            placesViewModel,
            navigateToAddPlace = {
                id ->
                navController.navigate(NavRoute.AddPlace.createRoute(id))
            }
        )
    }
}

fun AddNewPlaceScreen(navController: NavController,
                      navGraphBuilder: NavGraphBuilder,
                      mapViewModel: MapViewModel,
                      placesViewModel: PlaceViewModel){
    navGraphBuilder.composable(
        route = NavRoute.AddPlace.path.plus("/{placeId}"),
        arguments = listOf(
            navArgument("placeId"){
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

fun AddYourPlacesScreen(navController: NavController, navGraphBuilder: NavGraphBuilder, placesViewModel: PlaceViewModel
){
    navGraphBuilder.composable(
        route = NavRoute.YourPlaces.path
    ){
        YourPlacesScreen(
            placesViewModel,
            navigateToMap ={
                navController.navigate(NavRoute.MapScreen.path)
            }
        )
    }
}

fun AddSignUpScreen(navController: NavController, navGraphBuilder: NavGraphBuilder, googleSignInClient: GoogleSignInClient
){
    navGraphBuilder.composable(
        route = NavRoute.SignUp.path
    ){
        SignupScreen(
            googleSignInClient,
            navigateToMap ={
                navController.navigate(NavRoute.MapScreen.path)
            },
            navigateToLogin = {
                navController.navigate(NavRoute.Login.path)
            }
        )
    }
}

fun AddLoginScreen(navController: NavController, navGraphBuilder: NavGraphBuilder, googleSignInClient: GoogleSignInClient
){
    navGraphBuilder.composable(
        route = NavRoute.Login.path
    ){
        LoginScreen(
            googleSignInClient,
            navigateToMap = {
                navController.navigate(NavRoute.MapScreen.path)
            },
            navigateToSignUp = {
                navController.navigate(NavRoute.SignUp.path)
            }
        )
    }
}
