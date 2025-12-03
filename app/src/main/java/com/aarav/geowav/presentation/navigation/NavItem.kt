package com.aarav.geowav.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import com.aarav.geowav.R

sealed class NavItem(val name: String,val path: String, val icon: Int) {
    object Home: NavItem("Home", NavRoute.HomeScreen.path, R.drawable.navigation_arrow)
    object Activity: NavItem("Activity", NavRoute.ActivityScreen.path, R.drawable.activity)
    object YourPlaces: NavItem("Your Places", NavRoute.YourPlaces.path, R.drawable.map_pin_area)

//    object AddPlace: NavItem("Add Place", NavRoute.AddPlace.path, R.drawable.map_pin_area)
}