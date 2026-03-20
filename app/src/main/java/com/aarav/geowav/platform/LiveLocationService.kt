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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.aarav.geowav.R
import com.aarav.geowav.core.managers.KillSwitchManager
import com.aarav.geowav.core.tracking.StayPointTracker
import com.aarav.geowav.core.utils.FeatureAccess
import com.aarav.geowav.core.utils.ServiceState
import com.aarav.geowav.core.utils.formatTime
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.domain.repository.LiveLocationSharingRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.DateFormat
import javax.inject.Inject


@AndroidEntryPoint
class LiveLocationService : Service() {

    val ACTION_STOP = "ACTION_STOP_LIVE_LOCATION"

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
    lateinit var killSwitchManager: KillSwitchManager


    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var hasStartedSharing = false

    private val pushedStays = mutableSetOf<String>()

    private val stayPointTracker = StayPointTracker()

    private var userPlan: UserPlan = UserPlan.FREE
    private var sessionStartTime: Long = 0L

    private fun setSharingState(state: ServiceState) {
        sharedPreferences.edit(commit = true) {
            putString("live_location_state", state.name)
        }
    }


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        when (intent?.action) {
            ACTION_STOP -> {

                setSharingState(ServiceState.NOT_SHARING)

                if (::locationCallback.isInitialized) {
                    stopLocationUpdates()
                }

                stopForeground(true)
                Log.i("SERVICE", "stopped")

                val manager = getSystemService(NotificationManager::class.java)
                manager.cancel(3)

                stopSelf()
                return START_NOT_STICKY
            }
        }

        intent?.getStringExtra("USER_PLAN")?.let {
            userPlan = UserPlan.valueOf(it)
        }

        sessionStartTime = System.currentTimeMillis()

        return START_STICKY
    }


//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        if (!googleSignInClient.isLoggedIn()) {
//            stopSelf()
//            return START_NOT_STICKY
//        }
//
////        setSharingState(LiveLocationState.Sharing)
//        startForeground(1, createNotification())
//        startLocationUpdates()
//
//
//        return START_STICKY
//    }

    override fun onCreate() {
        super.onCreate()

        Log.i("SERVICE", "started")
        serviceScope.launch {

            killSwitchManager.fetchAndActivate()

            if (!killSwitchManager.isAppEnabled()) {
                stopSelf()
                return@launch
            }

            if (!googleSignInClient.isLoggedIn()) {
                stopSelf()
                return@launch
            }



            setSharingState(ServiceState.STARTING)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    3,
                    createNotification(),
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(3, createNotification())
            }


            sendLastKnownLocation()
            startLocationUpdates()

            killSwitchManager.observeAppEnabled()
                .collect { enabled ->
                    if (!enabled) {
                        shutdownService()
                    }
                }
        }
    }

//    private fun startLocationSharing() {
//        var first = true
//
//        getLocationUpdates()
//            .onEach { location ->
//                try {
//                    if (first) {
//                        liveLocationSharingRepository
//                            .startLiveLocationSharing(
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
        val username = googleSignInClient.getUserName()

        if (!hasStartedSharing) {
            liveLocationSharingRepository.startSharing(
                username,
                userId,
                location.latitude,
                location.longitude
            )
            hasStartedSharing = true
            setSharingState(ServiceState.SHARING)
        } else {
            liveLocationSharingRepository.updateLocation(
                userId,
                location.latitude,
                location.longitude
            )
        }

        stayPointTracker.onLocationUpdate(
            location.latitude,
            location.longitude,
            System.currentTimeMillis()
        )

        val stay = stayPointTracker.consumeQualifiedStay()

        stay?.let {

            val key = "${it.lat}_${it.lng}_${it.startedAt}"

            if (!pushedStays.contains(key)) {
                pushedStays.add(key)

                liveLocationSharingRepository.saveStayPoint(
                    userId,
                    it
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun sendLastKnownLocation() {
        val location = fusedLocationProviderClient.lastLocation.await()
        location?.let {
            sendLocation(it)
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
                if (!killSwitchManager
                        .isAppEnabled()
                ) {
                    return
                }

                val location = result.lastLocation ?: return

                serviceScope.launch {
                    try {
                        val elapsedTime = System.currentTimeMillis() - sessionStartTime

                        val maxDuration = FeatureAccess.locationSharingLimit(userPlan)


                        maxDuration?.let {

                            if(elapsedTime >= maxDuration) {
                                Log.i("SERVICE", "Session limit reached")

                                setSharingState(ServiceState.NOT_SHARING)

                                val intent = Intent("SESSION_LIMIT_REACHED").apply {
                                    `package` = packageName
                                }
                                sendBroadcast(intent)

                                stopLocationUpdates()
                                stopForeground(STOP_FOREGROUND_REMOVE)
                                stopSelf()

                                return@launch
                            }
                        }

                        sendLocation(location)
                    } catch (e: Exception) {
                        setSharingState(ServiceState.NOT_SHARING)
                        stopForeground(STOP_FOREGROUND_REMOVE)
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

    private fun createNotification(): Notification {
        val channelId = "live_location_channel"

        val channel = NotificationChannel(
            channelId,
            "Live Location",
            NotificationManager.IMPORTANCE_LOW
        )

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Sharing Live Location")
            .setContentText("Sharing your live location with your loved ones")
            .setSmallIcon(R.drawable.new_logo)
            .setOngoing(true)
            .build()

        return notification
    }

    override fun onDestroy() {

        stopForeground(STOP_FOREGROUND_REMOVE)

        if (::locationCallback.isInitialized) {
            stopLocationUpdates()
        }

        serviceScope.cancel()

        CoroutineScope(Dispatchers.IO + NonCancellable).launch {


            googleSignInClient.getUserId()?.let {
                val finalStays = stayPointTracker.finalizeAll()

                finalStays.forEach { stay ->

                    val key = "${stay.lat}_${stay.lng}_${stay.startedAt}"

                    if (!pushedStays.contains(key)) {

                        pushedStays.add(key)

                        liveLocationSharingRepository.saveStayPoint(
                            it,
                            stay
                        )
                    }
                }

                liveLocationSharingRepository.stopSharingLiveLocation(it)
            }
        }

        super.onDestroy()
    }

    private fun shutdownService() {

        stopLocationUpdates()

        stopForeground(STOP_FOREGROUND_REMOVE)

        stopSelf()
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }


}