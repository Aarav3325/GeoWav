package com.aarav.geowav

import android.Manifest
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresPermission
import com.aarav.geowav.data.geofence.GeofenceBroadcastReceiver
import com.aarav.geowav.data.geofence.GeofenceRepositoryImpl
import com.aarav.geowav.data.geofence.GeofencingVM
import com.aarav.geowav.domain.place.PlaceRepositoryImpl
import com.aarav.geowav.presentation.map.PlaceViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration


@HiltAndroidApp
class GeoWavApp : Application(), Configuration.Provider {

    @Inject
    lateinit var geofencingRepo: GeofenceRepositoryImpl

    @Inject
    lateinit var placeRepo: PlaceRepositoryImpl

    private lateinit var fusedClient: FusedLocationProviderClient

    @Inject lateinit var workerFactory: HiltWorkerFactory
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate() {
        super.onCreate()

        // Initialize FusedLocationProviderClient here
        fusedClient = LocationServices.getFusedLocationProviderClient(this)



        // Register geofences once app starts
//        CoroutineScope(Dispatchers.Default).launch {
//            placeRepo.getPlaces().firstOrNull()?.let { places ->
//                if (places.isNotEmpty()) {
//                    geofencingRepo.registerAllPlaces(places)
//                    Log.d("MYTAG", "Geofences registered for ${places.size} places")
//                }
//            }
//        }

//        startBackgroundLocationUpdates()
        Places.initializeWithNewPlacesApiEnabled(applicationContext, getString(R.string.maps_api))
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startBackgroundLocationUpdates() {

        val locationIntent = Intent(applicationContext, GeofenceBroadcastReceiver::class.java)
        val locationPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            locationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            10_000L
        )
            .setMinUpdateIntervalMillis(5_000L)
            .build()

        // Requires location permission, make sure granted
        fusedClient.requestLocationUpdates(locationRequest, locationPendingIntent)
    }
}
