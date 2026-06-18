package com.aarav.geowav.platform

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.PermissionChecker
import com.aarav.geowav.R
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.repository.GeofenceRepositoryImpl
import com.aarav.geowav.data.repository.PlaceRepositoryImpl
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceForegroundService : Service() {


    private val job = SupervisorJob()
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
 
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
 
    private var locationCallback: LocationCallback? = null


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startForegroundService()
            startLocationUpdates()
        }

        observePlaces()
    }

    private fun stopGeofenceCompletely() {

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        geofencingClient.removeGeofences(pendingIntent)

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
                .setRequestId(it.customName.ifEmpty { it.placeName })
                .setCircularRegion(it.latitude, it.longitude, it.radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
        }

        val geofencingRequest = GeofencingRequest.Builder().setInitialTrigger(0)
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
                                "Geofences added ${geofencingRequest.geofences}"
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

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun startForegroundService() {
        val channelId = "geo_channel"
        val channelName = "Geofence Alerts"
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

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
            .setSmallIcon(R.drawable.new_logo)
            .setOngoing(true) // Prevent swiping away
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()


        val permission = PermissionChecker.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.i("SERVICE", "geofence: started")
                startForeground(
                    1,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                Log.i("SERVICE", "geofence: started")
                startForeground(1, notification)
            }
        }
    }

    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            30_000L
        )
            .setMinUpdateIntervalMillis(10_000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                Log.d("GeofenceService", "Periodic background location ping: ${location.latitude}, ${location.longitude}")
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            request,
            locationCallback!!,
            mainLooper
        )
    }

    override fun onDestroy() {
        locationCallback?.let {
            fusedLocationProviderClient.removeLocationUpdates(it)
        }
        super.onDestroy()
        job.cancel()
    }
}
