package com.aarav.geowav.data.geofence

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresPermission
import com.aarav.geowav.data.place.Place
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GeofenceHelper @Inject constructor(@ApplicationContext val context: Context) {

//    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//    fun addGeofence(place: Place, onSuccess: () -> Unit = {}, onError: (Exception) -> Unit = {}) {
//            val geofence = Geofence.Builder()
//                .setRequestId(place.id)
//                .setCircularRegion(place.lat, place.lng, place.radius)
//                .setExpirationDuration(Geofence.NEVER_EXPIRE)
//                .setTransitionTypes(
//                    Geofence.GEOFENCE_TRANSITION_ENTER or
//                            Geofence.GEOFENCE_TRANSITION_EXIT
//                )
//                .build()
//
//
//            val geofencingRequest = GeofencingRequest.Builder()
//                .setInitialTrigger(
//                    GeofencingRequest.INITIAL_TRIGGER_ENTER or
//                            GeofencingRequest.INITIAL_TRIGGER_EXIT
//                )
//                .addGeofence(geofence)
//                .build()
//    }
//
//    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//    fun receiver(placeId : String, onSuccess: () -> Unit = {}, onError: (Exception) -> Unit = {},
//                 geofencingRequest : GeofencingRequest){
//        val pendingIntent = PendingIntent.getBroadcast(
//            context,
//            placeId.hashCode(),
//            Intent(context, GeofenceBroadcastReceiver::class.java),
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
//        )
//
//        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
//            .addOnSuccessListener { onSuccess() }
//            .addOnFailureListener { onError(it) }
//    })
    fun createGeofence(placeId: String, lat: Double, lng: Double, radius: Float): Geofence {
        return Geofence.Builder()
            .setRequestId(placeId)
            .setCircularRegion(lat, lng, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .build()
    }

    fun createGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(
                GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_EXIT
            )
            .addGeofence(geofence)
            .build()
    }

    fun getGeofencePendingIntent(context: Context): PendingIntent {
        val intent =  PendingIntent.getBroadcast(
            context.applicationContext,
            0,
            Intent(context.applicationContext, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )


        return intent
    }
}
