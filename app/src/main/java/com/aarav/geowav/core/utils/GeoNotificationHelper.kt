package com.aarav.geowav.core.utils

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aarav.geowav.MainActivity
import com.aarav.geowav.R
import com.aarav.geowav.presentation.navigation.NavRoute

sealed class NotificationType {
    object AcceptInvite : NotificationType()
    object NewInvite : NotificationType()
    object SharingStarted : NotificationType()
    object SharingStopped : NotificationType()
    object EmergencyStarted : NotificationType()
    object EmergencyStopped : NotificationType()
    object Trigger : NotificationType()
}

object GeoNotificationHelper {

    fun show(
        context: Context,
        channelId: String,
        title: String,
        message: String,
        type: NotificationType
    ) {

        val value = when (type) {
            NotificationType.AcceptInvite -> NavRoute.Circle.path
            NotificationType.NewInvite -> NavRoute.Circle.path
            NotificationType.SharingStarted -> NavRoute.ObserveUsers.path
            NotificationType.SharingStopped -> NavRoute.HomeScreen.path
            NotificationType.EmergencyStarted -> NavRoute.ObserveUsers.path
            NotificationType.EmergencyStopped -> NavRoute.HomeScreen.path
            NotificationType.Trigger -> NavRoute.HomeScreen.path
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }.putExtra("type", value)


        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            channelId
        )
            .setSmallIcon(R.drawable.new_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .build()


        val check =
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)

        Log.i("MYTAG", "show notification: $check")
        if (check == PackageManager.PERMISSION_GRANTED) {

            Log.i("MYTAG", "show notification")
            NotificationManagerCompat.from(context)
                .notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}