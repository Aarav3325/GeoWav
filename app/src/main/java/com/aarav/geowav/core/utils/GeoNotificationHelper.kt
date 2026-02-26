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
import androidx.core.content.PermissionChecker
import com.aarav.geowav.MainActivity
import com.aarav.geowav.R

object GeoNotificationHelper {

    fun show(
        context: Context,
        channelId: String,
        title: String,
        message: String
    ) {
        val intent = Intent(context, MainActivity::class.java)

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