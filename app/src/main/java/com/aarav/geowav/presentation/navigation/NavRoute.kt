package com.aarav.geowav.presentation.navigation

sealed class NavRoute(val path: String) {
    object HomeScreen : NavRoute("home")
    object ActivityScreen : NavRoute("activity")
    object Circle : NavRoute("circle")
    object LocationSharing : NavRoute("locationSharing")
    object ObserveUsers : NavRoute("observeUsers")
    object TimeLine : NavRoute("timeline") {
        fun createRoute(userId: String, name: String): String {
            return "timeline/$userId/$name"
        }
    }

    object TimelinePreview : NavRoute("timelinePreview") {
        fun createRoute(sessionId: String, userId: String): String {
            return "timelinePreview/$sessionId/$userId"
        }
    }

    object Paywall : NavRoute("paywall")

    object MapScreen : NavRoute("mapScreen")

    object AddPlace : NavRoute("addPlace") {
        fun createRoute(placeId: String): String {
            return "addPlace/$placeId"
        }
    }

    object ManualAddPlace : NavRoute("addPlace/manual") {
        fun createRoute(lat: Double, lng: Double): String {
            return "addPlace/manual/$lat/$lng"
        }
    }

    object YourPlaces : NavRoute("yourPlaces")
    object SignUp : NavRoute("signUp")
    object Login : NavRoute("login")
    object OnBoard : NavRoute("onBoard")
    object Settings : NavRoute("settings")
}
