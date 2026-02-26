package com.aarav.geowav

import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.aarav.geowav.data.repository.GeofenceRepositoryImpl
import com.aarav.geowav.data.repository.PlaceRepositoryImpl
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class GeoWavApp : Application(), Configuration.Provider {

    @Inject
    lateinit var geofencingRepo: GeofenceRepositoryImpl

    @Inject
    lateinit var placeRepo: PlaceRepositoryImpl

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()


    override fun onCreate() {
        super.onCreate()

        applicationContext.createGeoWavChannels()

        if(!Places.isInitialized()){
            Places.initializeWithNewPlacesApiEnabled(applicationContext, getString(R.string.maps_api))
        }
    }

    fun Context.createGeoWavChannels() {

        val circle = NotificationChannel(
            "circle_channel",
            "Circle & Invites",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Invites and circle updates"
        }

        val sharing = NotificationChannel(
            "sharing_channel",
            "Location Sharing",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Sharing start and stop alerts"
        }

        val emergency = NotificationChannel(
            "emergency_channel",
            "Emergency Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Emergency mode notifications"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 300, 500)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(circle)
        manager.createNotificationChannel(sharing)
        manager.createNotificationChannel(emergency)


        Log.i("MYTAG", "channels created")
    }
}