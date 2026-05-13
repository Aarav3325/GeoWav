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
            "Safety-focused movement visibility for the moments you choose to share.",
            R.drawable.new_logo,
            "No sharing starts without a clear action from you."
        ),
        OnBoardingPage(
            "Live Awareness",
            "GeoWav can show realtime movement updates during active sessions, so trusted people can understand what is happening.",
            R.drawable.navigation_arrow
        ),
        OnBoardingPage(
            "Location Access",
            "Location is used to power live movement updates, place alerts, and safety sessions when you turn those features on.",
            R.drawable.map_pin,
            "You can change access anytime in Android settings."
        ),
        OnBoardingPage(
            "Notifications",
            "Notifications help GeoWav tell you about invites, place alerts, sharing changes, and emergency activity.",
            R.drawable.bell,
            "You decide whether alerts are enabled."
        ),
        OnBoardingPage(
            "Background Access",
            "Background location keeps active safety sessions and place alerts working when GeoWav is not open.",
            R.drawable.gps,
            "GeoWav should always make active sharing visible."
        ),
        OnBoardingPage(
            "Privacy & Control",
            "Sharing should feel visible, reversible, and intentional. You should be able to see when sharing is active and stop it clearly.",
            R.drawable.lock,
            "GeoWav is built around consent, not hidden monitoring."
        ),
        OnBoardingPage(
            "Session Visibility",
            "Review movement from sessions you chose to keep, including route playback and important stops.",
            R.drawable.timeline,
            "Session history exists to provide context, not pressure."
        ),
        OnBoardingPage(
            "Set Up Access",
            "Next, GeoWav will explain each permission separately. You can enable access now or come back later from settings.",
            R.drawable.check,
            "Permission setup is progressive and optional."
        )
    )
}
