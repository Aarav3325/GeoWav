package com.aarav.geowav.presentation.onboard

import com.aarav.geowav.R

data class OnBoardingPage(val title: String, val description: String, val imageRes: Int)


object OnBoardContent {
    val pages = listOf(
        OnBoardingPage(
            "Live Location Sharing",
            "Stay connected with friends and family through real-time location sharing. See where everyone is on the map instantly.",
            R.drawable.navigation_arrow
        ),
        OnBoardingPage(
            "Track & Replay Your Sessions",
            "GeoWav records your movement and shows your routes on the map. Replay past sessions with timeline playback and see where you stopped along the way.",
            R.drawable.timeline
        ),
        OnBoardingPage(
            "Places & Smart Geofences",
            "Save important places and get automatic alerts when someone enters or leaves them. GeoWav also highlights stay points during your sessions.",
            R.drawable.gps
        )
    )
}