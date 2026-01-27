package com.aarav.geowav

import android.Manifest
import android.app.Application
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


        if(!Places.isInitialized()){
            Places.initializeWithNewPlacesApiEnabled(applicationContext, getString(R.string.maps_api))
        }
    }
}