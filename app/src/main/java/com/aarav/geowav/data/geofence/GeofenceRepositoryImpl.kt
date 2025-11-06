package com.aarav.geowav.data.geofence

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.aarav.geowav.data.place.Place
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GeofenceRepositoryImpl @Inject constructor(@ApplicationContext val context: Context,
    val geofencingClient: GeofencingClient, val geofenceHelper : GeofenceHelper
    ) {

//    @Inject
//    lateinit var geofencingClient: GeofencingClient
//    @Inject
//    lateinit var geofenceHelper : GeofenceHelper

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun registerAllPlaces(places: List<Place>) {

        val pendingIntent = geofenceHelper.getGeofencePendingIntent(context)
        for (place in places) {
            val geofence = geofenceHelper.createGeofence(place.placeId, place.latitude, place.longitude, place.radius)
            val request = geofenceHelper.createGeofencingRequest(geofence)

            geofencingClient.addGeofences(request, pendingIntent)
                .addOnSuccessListener {
                    Log.d("MYTAG", "${place.placeId} added")
                }
                .addOnFailureListener {
                    Log.e("MYTAG", "error in adding geofence")
                }
        }
    }
}