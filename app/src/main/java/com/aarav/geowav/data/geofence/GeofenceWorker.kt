package com.aarav.geowav.data.geofence

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.aarav.geowav.data.model.Component
import com.aarav.geowav.data.model.Language
import com.aarav.geowav.data.model.Parameter
import com.aarav.geowav.data.model.Template
import com.aarav.geowav.data.model.TemplateMessageRequest
import com.aarav.geowav.data.repository.MessageRepo
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
        val transitionType = when (transitionTypeRaw.uppercase()) {
            "ENTER" -> "reached"
            "EXIT" -> "left"
            else -> transitionTypeRaw.lowercase()
        }


        val userId = "user123"

        val timestamp = System.currentTimeMillis()
        val readableTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))

        val latitude = inputData.getDouble("latitude", 0.0)
        val longitude = inputData.getDouble("longitude", 0.0)


        val activityData = mapOf(
            "geofenceId" to geofenceId,
            "transitionType" to transitionType,
            "timestamp" to timestamp,
            "dateKey" to dateKey,       // yyyy-MM-dd
            "readableTime" to readableTime,
            "location" to mapOf(
                "latitude" to latitude,
                "longitude" to longitude
            )
        )


        // Changes - 1. phoneNumber field 2. fetch user name

        val templateRequest = TemplateMessageRequest(
            messaging_product = "whatsapp",
            to = "919558030582",
            type = "template",
            template = Template(
                name = "geowav_location_update",
                language = Language("en"),
                components = listOf(
                    Component(
                        type = "body",
                        parameters = listOf(
                            Parameter("text", "user_name", "Aarav"),
                            Parameter("text", "action", "$transitionType $geofenceId"),
                            Parameter("text", "time", readableTime)
                        )
                    )
                )
            )
        )


        val messageRepo = MessageRepo()
        messageRepo.sendMessageSync(templateRequest, activityData)

        return Result.success()


    }
}
