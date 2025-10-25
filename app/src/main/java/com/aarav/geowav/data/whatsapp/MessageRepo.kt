package com.aarav.geowav.data.whatsapp

import android.util.Log
import com.aarav.geowav.data.retrofit.MessageAPI
import com.aarav.geowav.data.retrofit.RetrofitInstance
import com.aarav.geowav.data.retrofit.TemplateMessageRequest
import com.aarav.geowav.data.retrofit.WhatsAppMessageResponse
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class MessageRepo {


    fun sendMessage(request: TemplateMessageRequest, activityData: Map<String, Any>) {
        Log.i("MYTAG", "sendMessage called")

        val messageAPI = RetrofitInstance.getMessagesAPI()
        messageAPI.postMessage(request).enqueue(object : Callback<WhatsAppMessageResponse> {
            override fun onResponse(
                call: Call<WhatsAppMessageResponse>,
                response: Response<WhatsAppMessageResponse>
            ) {
                Log.i("MYTAG", "WhatsApp message sent: ${response.body()}")

                if (response.isSuccessful) {
                    Log.i("MYTAG", "WhatsApp message sent: ${response.body()}")

                    // Write to Firebase here
                    val firebaseDatabase = FirebaseDatabase.getInstance()
                    firebaseDatabase.getReference("geofence_activity")
                        .child("user123")
                        .push()
                        .setValue(activityData)
                        .addOnSuccessListener { Log.i("MYTAG", "Firebase write success") }
                        .addOnFailureListener { e -> Log.e("MYTAG", "Firebase write failed", e) }
                } else {
                    Log.e("MYTAG", "WhatsApp message failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<WhatsAppMessageResponse>, t: Throwable) {
                Log.e("MYTAG", "WhatsApp API error", t)
            }
        })
    }

    fun sendMessageSync(request: TemplateMessageRequest, activityData: Map<String, Any>) {

        Log.i("MYTAG", "sendMessageSync called")
        val messageAPI = RetrofitInstance.getMessagesAPI()
        try {
            val response = messageAPI.postMessage(request).execute()
            if (response.isSuccessful) {
                Log.i("MYTAG", "WhatsApp message sent: ${response.body()}")

                val firebaseDatabase = FirebaseDatabase.getInstance()
                firebaseDatabase.getReference("geofence_activity")
                    .child("user123")
                    .push()
                    .setValue(activityData)
                    .addOnSuccessListener { Log.i("MYTAG", "Firebase write success") }
                    .addOnFailureListener { e -> Log.e("MYTAG", "Firebase write failed", e) }
            } else {
                Log.e("MYTAG", "WhatsApp message failed: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("MYTAG", "WhatsApp API error", e)
        }
    }



}