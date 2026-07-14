package com.aarav.geowav.data.model

data class PaywallConfig(
    val offeringId: String,
    val title: String,
    val subtitle: String,
    val launchOfferEnabled: Boolean,
    val showLaunchBadge: Boolean,
    val launchBadgeText: String,
    val trialMessage: String
)
