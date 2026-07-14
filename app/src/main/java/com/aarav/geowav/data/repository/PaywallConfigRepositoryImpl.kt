package com.aarav.geowav.data.repository

import android.util.Log
import com.aarav.geowav.data.model.PaywallConfig
import com.aarav.geowav.domain.repository.PaywallConfigRepository
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class PaywallConfigRepositoryImpl @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) : PaywallConfigRepository {

    private val defaults = mapOf(
        "paywall_offering_id" to "",
        "paywall_title" to "Unlock smarter tracking",
        "paywall_subtitle" to "Replay journeys, track longer, and share with your inner circle.",
        "launch_offer_enabled" to false,
        "show_launch_badge" to false,
        "launch_badge_text" to "🎉 Launch Offer",
        "trial_message" to "7-Day Free Trial"
    )

    private val _paywallConfig = MutableStateFlow(getActiveConfig())
    override val paywallConfig: StateFlow<PaywallConfig> = _paywallConfig.asStateFlow()

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 5
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(defaults)

        remoteConfig.activate().addOnCompleteListener {
            _paywallConfig.value = getActiveConfig()
        }

        try {
            remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
                override fun onUpdate(configUpdate: ConfigUpdate) {
                    remoteConfig.activate().addOnCompleteListener {
                        _paywallConfig.value = getActiveConfig()
                        Log.d("PaywallConfigRepo", "Remote Config updated and activated")
                    }
                }

                override fun onError(error: FirebaseRemoteConfigException) {
                    Log.e("PaywallConfigRepo", "Error matching config updates", error)
                }
            })
        } catch (e: Exception) {
            Log.e("PaywallConfigRepo", "Failed to register config update listener", e)
        }

        CoroutineScope(Dispatchers.IO).launch {
            refreshConfig()
        }
    }

    override suspend fun refreshConfig(): Result<PaywallConfig> = suspendCancellableCoroutine { continuation ->
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newConfig = getActiveConfig()
                    _paywallConfig.value = newConfig
                    if (continuation.isActive) continuation.resume(Result.success(newConfig))
                } else {
                    val exception = task.exception ?: Exception("Remote Config fetch failed")
                    if (continuation.isActive) continuation.resume(Result.failure(exception))
                }
            }
    }

    private fun getActiveConfig(): PaywallConfig {
        val offeringId = remoteConfig.getString("paywall_offering_id").ifBlank { "" }
        val title = remoteConfig.getString("paywall_title").ifBlank { "Unlock smarter tracking" }
        val subtitle = remoteConfig.getString("paywall_subtitle").ifBlank { "Replay journeys, track longer, and share with your inner circle." }
        val launchOfferEnabled = remoteConfig.getBoolean("launch_offer_enabled")
        val showLaunchBadge = remoteConfig.getBoolean("show_launch_badge")
        val launchBadgeText = remoteConfig.getString("launch_badge_text").ifBlank { "🎉 Launch Offer" }
        val trialMessage = remoteConfig.getString("trial_message").ifBlank { "7-Day Free Trial" }

        return PaywallConfig(
            offeringId = offeringId,
            title = title,
            subtitle = subtitle,
            launchOfferEnabled = launchOfferEnabled,
            showLaunchBadge = showLaunchBadge,
            launchBadgeText = launchBadgeText,
            trialMessage = trialMessage
        )
    }
}
