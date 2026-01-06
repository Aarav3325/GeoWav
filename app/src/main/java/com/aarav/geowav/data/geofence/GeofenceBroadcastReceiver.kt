package com.aarav.geowav.data.geofence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.WorkManager
import com.aarav.geowav.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class GeofenceBroadcastReceiver : BroadcastReceiver() {

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
            showNotification(context, "GeoWav", "${transitionType.uppercase()} ${geofence.requestId}")

            val inputData = androidx.work.workDataOf(
                "geofenceId" to geofence.requestId,
                "transitionType" to transitionType,
                "latitude" to geofence.latitude,
                "longitude" to geofence.longitude,
            )

            val workRequest = androidx.work.OneTimeWorkRequestBuilder<GeofenceWorker>()
                .setInputData(inputData)
                .build()
            Log.i("MYTAG", "Enqueueing GeofenceWorker for ${geofence.requestId}")
            WorkManager.getInstance(context).enqueue(workRequest)
            Log.i("MYTAG", "Worker enqueued")

            Log.i("MYTAG", "${transitionType.uppercase()} ${geofence.requestId} at ${System.currentTimeMillis()}")
        }

    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "geo_channel"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Geofence Alerts", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.navigation_arrow)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

}