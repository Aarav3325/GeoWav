package com.aarav.geowav.platform

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.PermissionChecker
import com.aarav.geowav.R
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.security.Permissions
import java.util.jar.Manifest
import javax.inject.Inject

class LiveLocationService: Service() {

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    lateinit var locationCallback : LocationCallback


    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()

        if(googleSignInClient.isLoggedIn()) {
            startForeground(1, createNotification())
            getLocationUpdates()
        }

    }

    @SuppressLint("MissingPermission")
    private fun getLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateIntervalMillis(2_000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
//                result.lastLocation?.let { trySend(it) }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, null)
//        awaitClose { fusedLocationProviderClient.removeLocationUpdates(callback) }
    }

    private fun createNotification(): Notification {
        val channelId = "live_location_channel"

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Live Location",
                NotificationManager.IMPORTANCE_LOW
            )

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val notification =  NotificationCompat.Builder(this, channelId)
            .setContentTitle("Sharing Live Location")
            .setContentText("Sharing your live location with your loved ones")
            .setSmallIcon(R.drawable.new_logo)
            .setOngoing(true)
            .build()

        return notification
    }

    override fun onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

}