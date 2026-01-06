package com.aarav.geowav.data.geofence

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.aarav.geowav.R
import com.aarav.geowav.data.place.Place
import com.aarav.geowav.data.repository.GeofenceRepositoryImpl
import com.aarav.geowav.domain.authentication.GoogleSignInClient
import com.aarav.geowav.domain.place.PlaceRepositoryImpl
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceForegroundService : Service() {


    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    @Inject
    lateinit var geofencingClient: GeofencingClient

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    @Inject
    lateinit var geofenceHelper: GeofenceHelper

    @Inject
    lateinit var geofenceRepositoryImpl: GeofenceRepositoryImpl

    @Inject
    lateinit var placeRepositoryImpl: PlaceRepositoryImpl

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()

        if (googleSignInClient.isLoggedIn()) {
            observePlaces()
        }

    }

    private fun observePlaces() {
        scope.launch {
            placeRepositoryImpl.getPlaces().collect { places ->
                registerGeofences(places)
            }
        }
    }

    private fun registerGeofences(list: List<Place>) {
        if (list.isEmpty()) return

        val geofenceList = list.map {
            Geofence.Builder()
                .setRequestId(it.placeName)
                .setCircularRegion(it.latitude, it.longitude, it.radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
        }

        val geofencingRequest = GeofencingRequest.Builder().setInitialTrigger(
            GeofencingRequest.INITIAL_TRIGGER_ENTER
            //or GeofencingRequest.INITIAL_TRIGGER_EXIT
        )
            .addGeofences(geofenceList)
            .build()

        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, Intent(this, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            geofencingClient.removeGeofences(pendingIntent)
                .addOnSuccessListener {
                    geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                        .addOnSuccessListener {
                            Log.d(
                                "GeofenceService",
                                "Geofences added ${geofencingRequest.geofences.size}"
                            )
                        }
                        .addOnFailureListener {
                            Log.e(
                                "GeofenceService",
                                "Failed to add geofences",
                                it
                            )
                        }
                }
                .addOnFailureListener {
                    Log.e(
                        "GeofenceService",
                        "Failed to remove old geofences",
                        it
                    )
                }
        }
    }

    private fun startForegroundService() {
        val channelId = "geo_channel"
        val channelName = "Geofence Alerts"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("GeoWav Active")
            .setContentText("Monitoring geofence events")
            .setSmallIcon(R.drawable.navigation_arrow)
            .setOngoing(true) // Prevent swiping away
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


}
