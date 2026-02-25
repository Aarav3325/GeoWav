package com.aarav.geowav.core.managers

import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class KillSwitchManager @Inject constructor(
    val firebaseRemoteConfig: FirebaseRemoteConfig
) {

    suspend fun fetchAndActivate(): Boolean {
        return firebaseRemoteConfig.fetchAndActivate().await()
    }

    fun isAppEnabled(): Boolean {
        return firebaseRemoteConfig.getBoolean("app_enabled")
    }

    fun observeAppEnabled(): Flow<Boolean> = callbackFlow {

        // Emit current value immediately
        trySend(firebaseRemoteConfig.getBoolean("app_enabled"))

        val registration = firebaseRemoteConfig.addOnConfigUpdateListener(
            object : ConfigUpdateListener {

                override fun onUpdate(configUpdate: ConfigUpdate) {

                    if (configUpdate.updatedKeys.contains("app_enabled")) {

                        firebaseRemoteConfig.activate()
                            .addOnCompleteListener {
                                trySend(firebaseRemoteConfig.getBoolean("app_enabled"))
                            }
                    }
                }

                override fun onError(error: FirebaseRemoteConfigException) {

                }
            }
        )

        awaitClose {
            registration.remove()
        }

    }.distinctUntilChanged()
}