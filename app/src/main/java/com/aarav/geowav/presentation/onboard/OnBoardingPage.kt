package com.aarav.geowav.presentation.onboard

import com.aarav.geowav.R

data class OnBoardingPage(val title: String, val description: String, val imageRes: Int)


object OnBoardContent {
    val pages = listOf(
        OnBoardingPage(
            "Stay Connected in Real-Time",
            "GeoWav keeps you updated about your friends and loved ones with live location sharing. See who’s nearby and never miss a moment.",
            R.drawable.gps
        ),
        OnBoardingPage(
            "Smart Geofences & Alerts",
            "Set places that matter — home, school, or office. Get notified when someone arrives or leaves, automatically and effortlessly.",
            R.drawable.navigation_arrow
        ),
        OnBoardingPage(
            "Your Safety, Our Priority",
            "GeoWav values privacy and security. Your location is shared only with people you trust. Stay safe while staying connected.",
            R.drawable.vault
        ),
    )
}