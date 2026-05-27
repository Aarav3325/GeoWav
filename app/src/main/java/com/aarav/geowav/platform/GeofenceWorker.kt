package com.aarav.geowav.platform

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.aarav.geowav.data.model.ActivityTransition
import com.aarav.geowav.data.model.MovementActivityRecord
import com.aarav.geowav.data.repository.ActivityWriteRepository
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GeofenceWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {


        Log.i("MYTAG", "worker called")

        val geofenceId = inputData.getString("geofenceId") ?: return Result.failure()
        val transitionTypeRaw = inputData.getString("transitionType") ?: return Result.failure()
        val normalizedTransition = ActivityTransition.fromRaw(transitionTypeRaw)
            ?: return Result.failure()

        val timestamp = System.currentTimeMillis()
        val readableTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))

        val latitude = inputData.getDouble("latitude", 0.0)
        val longitude = inputData.getDouble("longitude", 0.0)



        if (!FirebaseRemoteConfig.getInstance().getBoolean("app_enabled")) {
            return Result.success()
        }


        val activity = MovementActivityRecord(
            placeName = geofenceId,
            transition = normalizedTransition,
            timestamp = timestamp,
            dateKey = dateKey,
            readableTime = readableTime,
            latitude = latitude,
            longitude = longitude
        )

        return try {
            ActivityWriteRepository().recordMovementActivity(activity)
            Result.success()
        } catch (error: Exception) {
            Log.e("MYTAG", "Failed to record geofence activity", error)
            Result.retry()
        }

    }
}
