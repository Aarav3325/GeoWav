package com.aarav.geowav.data.geofence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aarav.geowav.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.database.FirebaseDatabase

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    //    override fun onReceive(context: Context, intent: Intent) {
//
//        if (intent.action != "GEOFENCE_EVENT") return
//
//
//        Log.d("MYTAG", "Receiver triggered: ${intent.action}, extras: ${intent.extras}")
//
//        val geofencingEvent = GeofencingEvent.fromIntent(intent)
//        if (geofencingEvent == null) {
//            Log.e("MYTAG", "GeofencingEvent is null")
//            return
//        }
//
//        if (geofencingEvent.hasError()) {
//            Log.e("MYTAG", "Geofencing error: ${geofencingEvent.errorCode}")
//            return
//        }
//
//        geofencingEvent.triggeringGeofences?.forEach {
//            Log.i("MYTAG", "Triggered geofence: ${it.requestId}")
//        }
//
//        when(geofencingEvent.geofenceTransition){
//            Geofence.GEOFENCE_TRANSITION_ENTER -> {
//                geofencingEvent.triggeringGeofences?.forEach {
//                    geofence -> Log.i("MYTAG", "Entered ${geofence.requestId} at ${System.currentTimeMillis()}")
//                }
//            }
//
//            Geofence.GEOFENCE_TRANSITION_EXIT -> {
//                geofencingEvent.triggeringGeofences?.forEach {
//                        geofence -> Log.i("MYTAG", "Left ${geofence.requestId}")
//                }
//            }
//        }
//
//    }

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

            // Enqueue WorkManager
            val inputData = androidx.work.workDataOf(
                "geofenceId" to geofence.requestId,
                "transitionType" to transitionType
            )

            val workRequest = androidx.work.OneTimeWorkRequestBuilder<GeofenceWorker>()
                .setInputData(inputData)
                .build()

            androidx.work.WorkManager.getInstance(context).enqueue(workRequest)

            // Log
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