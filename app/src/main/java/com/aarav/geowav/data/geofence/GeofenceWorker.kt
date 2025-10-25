package com.aarav.geowav.data.geofence

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.aarav.geowav.data.retrofit.Component
import com.aarav.geowav.data.retrofit.Language
import com.aarav.geowav.data.retrofit.Parameter
import com.aarav.geowav.data.retrofit.RetrofitInstance
import com.aarav.geowav.data.retrofit.Template
import com.aarav.geowav.data.retrofit.TemplateMessageRequest
import com.aarav.geowav.data.retrofit.WhatsAppMessageResponse
import com.aarav.geowav.data.whatsapp.MessageRepo
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class GeofenceWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {

        //2️⃣ Send WhatsApp message asynchronously
       // val messageRepo = MessageRepo(RetrofitInstance.getMessagesAPI)
        //val messageRepo = MessageRepo(RetrofitInstance.getMessagesAPI)

        //val firebaseDatabase = FirebaseDatabase.getInstance()

        Log.i("MYTAG", "worker called")

        val geofenceId = inputData.getString("geofenceId") ?: return Result.failure()
        val transitionTypeRaw = inputData.getString("transitionType") ?: return Result.failure()
        val transitionType = when (transitionTypeRaw.uppercase()) {
            "ENTER" -> "reached"
            "EXIT" -> "left"
            else -> transitionTypeRaw.lowercase() // fallback for unexpected values
        }


//        val userId = firebaseAuth.currentUser?.uid ?: return Result.failure()
        val userId = "user123"

        val timestamp = System.currentTimeMillis()
        val readableTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))

        val latitude = inputData.getDouble("latitude", 0.0)
        val longitude = inputData.getDouble("longitude", 0.0)

//        val phoneNumber = inputData.getString("phoneNumber") ?: return Result.failure()
//        val userName = inputData.getString("userName") ?: return Result.failure()

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

//        firebaseDatabase.getReference("geofence_activity")
//            .child(userId)
//            .push()
//            .setValue(activityData)
//            .addOnSuccessListener { Log.i("MYTAG", "Firebase write success") }
//            .addOnFailureListener { e -> Log.e("MYTAG", "Firebase write failed", e) }

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

       // val firebaseDatabase = FirebaseDatabase.getInstance()
//        firebaseDatabase.getReference("geofence_activity")
//            .child(userId)
//            .push()
//            .setValue(activityData)
//            .addOnSuccessListener { Log.i("MYTAG", "Firebase write success") }
//            .addOnFailureListener { e -> Log.e("MYTAG", "Firebase write failed", e) }

// Send WhatsApp message asynchronously
        val messageRepo = MessageRepo()
        messageRepo.sendMessageSync(templateRequest, activityData)

// Worker returns success immediately
        return Result.success()

       // Log.d("Request", templateRequest.toString())

//        try {
//            messageRepo.sendMessage(templateRequest)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return Result.retry()
//        }

    }
}
