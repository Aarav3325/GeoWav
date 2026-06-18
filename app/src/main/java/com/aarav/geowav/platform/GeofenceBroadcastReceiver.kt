package com.aarav.geowav.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    // detect geofence activity and send notification to connection via Whatsapp Cloud API
    override fun onReceive(context: Context, intent: Intent) {

        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) {
            Log.e("MYTAG", "Geofencing error: ${geofencingEvent.errorCode}")
            return
        }

        val transitionType = when(geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "enter"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "exit"
            else -> return
        }

        geofencingEvent.triggeringGeofences?.forEach { geofence ->
            val inputData = workDataOf(
                "geofenceId" to geofence.requestId,
                "transitionType" to transitionType,
                "latitude" to geofence.latitude,
                "longitude" to geofence.longitude,
            )

            val workRequest = OneTimeWorkRequestBuilder<GeofenceWorker>()
                .setInputData(inputData)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            Log.i("MYTAG", "Enqueueing GeofenceWorker for ${geofence.requestId}")
            WorkManager.getInstance(context).enqueue(workRequest)
            Log.i("MYTAG", "Worker enqueued")

            Log.i("MYTAG", "${transitionType.uppercase()} ${geofence.requestId} at ${System.currentTimeMillis()}")
        }

    }

}
