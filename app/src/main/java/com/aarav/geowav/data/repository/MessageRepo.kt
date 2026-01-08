package com.aarav.geowav.data.repository

import android.util.Log
import com.aarav.geowav.data.model.TemplateMessageRequest
import com.aarav.geowav.data.model.WhatsAppMessageResponse
import com.aarav.geowav.data.datasource.retrofit.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "user123"
                firebaseDatabase.getReference("geofence_activity")
                    .child(userId)
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