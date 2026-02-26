package com.aarav.geowav.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.GeoNotificationHelper
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.data.repository.NotificationRepositoryImpl
import com.aarav.geowav.domain.repository.CircleRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationService : Service() {

    @Inject
    lateinit var notificationRepository: NotificationRepositoryImpl

    @Inject
    lateinit var circleRepository: CircleRepository

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()


        CoroutineScope(Dispatchers.IO).launch {

            when (val members = circleRepository.getAcceptedLovedOnes(
                firebaseAuth.currentUser?.uid ?: return@launch
            )) {
                is Resource.Success -> {
                    notificationRepository.startListening(members.data?.map { it.id } ?: emptyList())
                }

                else -> Unit
            }

            notificationRepository.events.collect {
                handleEvent(it)
            }
        }
    }

    private fun handleEvent(event: SocialEvent) {
        when (event) {

            is SocialEvent.InviteReceived -> {
                GeoNotificationHelper.show(
                    this,
                    "circle_channel",
                    "New Invite",
                    "${event.senderName} invited you to their circle"
                )
            }

            is SocialEvent.InviteAccepted -> {
                GeoNotificationHelper.show(
                    this,
                    "circle_channel",
                    "Circle Update",
                    "${event.userName} accepted your invite and joined your circle"
                )
            }

            is SocialEvent.SharingStarted -> {
                GeoNotificationHelper.show(
                    this,
                    "sharing_channel",
                    "Location Update",
                    "${event.userName} started sharing location"
                )
            }

            is SocialEvent.SharingStopped -> {
                GeoNotificationHelper.show(
                    this,
                    "sharing_channel",
                    "Location Update",
                    "${event.userName} stopped sharing location"
                )
            }

            else -> Unit
        }
    }

    private fun startForegroundServiceNotification() {

        val channel = NotificationChannel(
            "service_channel",
            "Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(
            this,
            "service_channel"
        )
            .setSmallIcon(R.drawable.new_logo)
            .setContentTitle("GeoWav Running")
            .setContentText("Monitoring circle and share updates")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                1,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(1, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationRepository.stopListening()
    }
}

