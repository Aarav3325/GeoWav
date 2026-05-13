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
import com.aarav.geowav.core.managers.StrictModeHelper
import com.aarav.geowav.data.repository.GeofenceRepositoryImpl
import com.aarav.geowav.data.repository.PlaceRepositoryImpl
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.getCustomerInfoWith
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class GeoWavApp : Application(), Configuration.Provider {


    val TAG = "RevenueCat"
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
        //StrictModeHelper.enable()

        if(!Places.isInitialized()){
            Places.initializeWithNewPlacesApiEnabled(applicationContext, BuildConfig.GOOGLE_MAPS_API_KEY)
        }

        Purchases.logLevel = LogLevel.DEBUG // Change to LogLevel.ERROR for release
        Purchases.configure(
            PurchasesConfiguration.Builder(
                context = this,
                apiKey = BuildConfig.REVENUE_CAT_API_KEY
            ).build()
        )

//        Log.i(TAG, "Before customer info")
//
//        Purchases.sharedInstance.getCustomerInfoWith(
//            onSuccess = {
//                Log.i(TAG, "Customer info success")
//            },
//            onError = {
//                Log.e(TAG, "Customer info error")
//            }
//        )
//
//        Log.i(TAG, "After customer info")
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


        Log.i("MYTAG", "channels created ")
    }
}