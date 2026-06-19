package com.aarav.geowav.presentation.onboard

import com.aarav.geowav.R

data class OnBoardingPage(
    val title: String,
    val description: String,
    val imageRes: Int,
    val reassurance: String? = null
)


object OnBoardContent {
    val pages = listOf(
        OnBoardingPage(
            "GeoWav",
            "Real-time movement updates with your trusted circles. Start active sharing sessions so the people who matter know you are safe.",
            R.drawable.new_logo,
            "No sharing starts without a clear action from you."
        ),
        OnBoardingPage(
            "Smart Place Alerts",
            "Set up custom places and receive automatic notifications when circle members arrive or leave, keeping you connected in the background.",
            R.drawable.map_pin,
            "Background location ensures alerts work even when the app is closed."
        ),
        OnBoardingPage(
            "History & Setup",
            "Review details of your saved sessions with route playback, and manage all your settings and system permissions in one place.",
            R.drawable.timeline,
            "Your history is yours to control, and permission setup is optional."
        )
    )
}
