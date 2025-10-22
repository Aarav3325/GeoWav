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
import com.aarav.geowav.presentation.map.AddPlaceScreen
import com.aarav.geowav.presentation.map.MapScreen
import com.aarav.geowav.presentation.map.MapViewModel
import com.aarav.geowav.presentation.map.PlaceViewModel
import com.aarav.geowav.presentation.map.YourPlacesScreen
import com.google.android.gms.maps.MapView

@Composable
fun NavGraph(navHostController: NavHostController,
             location: Pair<Double, Double>?,
             mapViewModel: MapViewModel,
             placesViewModel: PlaceViewModel) {

    NavHost(navController = navHostController, startDestination = NavRoute.MapScreen.path){
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
