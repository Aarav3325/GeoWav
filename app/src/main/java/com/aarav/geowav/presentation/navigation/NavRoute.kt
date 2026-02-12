package com.aarav.geowav.presentation.navigation

sealed class NavRoute(val path: String) {
    object HomeScreen : NavRoute("home")
    object ActivityScreen : NavRoute("activity")
    object Circle : NavRoute("circle")

    object MapScreen : NavRoute("mapScreen")

    object AddPlace : NavRoute("addPlace") {
        fun createRoute(placeId: String): String {
            return "addPlace/$placeId"
        }
    }

    object YourPlaces : NavRoute("yourPlaces")
    object SignUp : NavRoute("signUp")
    object Login : NavRoute("login")
    object OnBoard : NavRoute("onBoard")
    object Settings : NavRoute("settings")
}