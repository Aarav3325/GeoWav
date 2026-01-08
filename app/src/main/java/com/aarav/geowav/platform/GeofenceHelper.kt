package com.aarav.geowav.platform

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GeofenceHelper @Inject constructor(@ApplicationContext val context: Context) {

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
        val intent = PendingIntent.getBroadcast(
            context.applicationContext,
            0,
            Intent(context.applicationContext, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )


        return intent
    }
}
