package com.aarav.geowav.data.geofence

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class GeofenceWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {

         val firebaseDatabase = FirebaseDatabase.getInstance()

        Log.i("MYTAG", "worker called")

        val geofenceId = inputData.getString("geofenceId") ?: return Result.failure()
        val transitionType = inputData.getString("transitionType") ?: return Result.failure()

//        val userId = firebaseAuth.currentUser?.uid ?: return Result.failure()
        val userId = "user123"

        val timestamp = System.currentTimeMillis()
        val readableTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

        val latitude = inputData.getDouble("latitude", 0.0)
        val longitude = inputData.getDouble("longitude", 0.0)

        val activityData = mapOf(
            "geofenceId" to geofenceId,
            "transitionType" to transitionType,
            "timestamp" to timestamp,
            "readableTime" to readableTime,
            "location" to mapOf(
                "latitude" to latitude,
                "longitude" to longitude
            )
        )

        firebaseDatabase.getReference("geofence_activity")
            .child(userId)
            .push()
            .setValue(activityData)
            .addOnSuccessListener { Log.i("MYTAG", "Firebase write success") }
            .addOnFailureListener { e -> Log.e("MYTAG", "Firebase write failed", e) }


        return Result.success()
    }
}
