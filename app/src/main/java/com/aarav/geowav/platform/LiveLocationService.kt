package com.aarav.geowav.platform

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.LiveLocationState
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.domain.repository.LiveLocationSharingRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class LiveLocationService : Service() {

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    lateinit var locationCallback: LocationCallback

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject
    lateinit var liveLocationSharingRepository: LiveLocationSharingRepository


    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private fun setSharingState(state: LiveLocationState) {
        sharedPreferences.edit {
            putString("live_location_state", state::class.simpleName)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!googleSignInClient.isLoggedIn()) {
            stopSelf()
            return START_NOT_STICKY
        }

        setSharingState(LiveLocationState.Sharing)
        startForeground(1, createNotification())
        startLocationUpdates()


        return START_STICKY
    }

//    override fun onCreate() {
//        super.onCreate()
//
//        if (googleSignInClient.isLoggedIn()) {
//            setSharingState(LiveLocationState.Starting)
//            startForeground(1, createNotification())
//            getLocationUpdates()
//        }
//
//    }

//    private fun startLocationSharing() {
//        var first = true
//
//        getLocationUpdates()
//            .onEach { location ->
//                try {
//                    if (first) {
//                        liveLocationSharingRepository
//                            .startSharing(
//                                googleSignInClient.getUserId(),
//                                location.latitude,
//                                location.longitude
//                            )
//                        setSharingState(LiveLocationState.Sharing)
//                        first = false
//                    } else {
//                        liveLocationSharingRepository
//                            .updateLocation(
//                                googleSignInClient.getUserId(),
//                                location.latitude,
//                                location.longitude
//                            )
//                    }
//                } catch (e: Exception) {
//                    setSharingState(LiveLocationState.Error("Failed to share live location"))
//                    stopSelf()
//                }
//            }
//            .launchIn(serviceScope)
//    }

    private suspend fun sendLocation(location: Location) {
        val userId = googleSignInClient.getUserId()

        val isActive = liveLocationSharingRepository
            .isLiveLocationActive(userId)

        if (!isActive) {
            liveLocationSharingRepository.startSharing(
                userId,
                location.latitude,
                location.longitude
            )
            setSharingState(LiveLocationState.Sharing)
        } else {
            liveLocationSharingRepository.updateLocation(
                userId,
                location.latitude,
                location.longitude
            )
        }
    }


    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5_000L
        )
            .setMinUpdateIntervalMillis(2_000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return

                serviceScope.launch {
                    try {
                        sendLocation(location)
                    } catch (e: Exception) {
                        setSharingState(
                            LiveLocationState.Error("Failed to share live location")
                        )
                        stopSelf()
                    }
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            request,
            locationCallback,
            mainLooper
        )
    }


//    @SuppressLint("MissingPermission")
//    private fun getLocationUpdates() {
//        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
//            .setMinUpdateIntervalMillis(2_000L)
//            .build()
//
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(result: LocationResult) {
//                val location = result.lastLocation ?: return
////                result.lastLocation?.let { trySend(it) }
//            }
//        }
//
//        fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, null)
////        awaitClose { fusedLocationProviderClient.removeLocationUpdates(callback) }
//    }

    private fun createNotification(): Notification {
        val channelId = "live_location_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Live Location",
                NotificationManager.IMPORTANCE_LOW
            )

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Sharing Live Location")
            .setContentText("Sharing your live location with your loved ones")
            .setSmallIcon(R.drawable.new_logo)
            .setOngoing(true)
            .build()

        return notification
    }

    override fun onDestroy() {

        fusedLocationProviderClient
            .removeLocationUpdates(locationCallback)

        serviceScope.launch {
            liveLocationSharingRepository
                .stopSharingLiveLocation(googleSignInClient.getUserId())
        }

        setSharingState(LiveLocationState.NotSharing)
        serviceScope.cancel()

        super.onDestroy()
    }



}