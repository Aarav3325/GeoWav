package com.aarav.geowav.data.repository

import android.util.Log
import com.aarav.geowav.data.model.TemplateMessageRequest
import com.aarav.geowav.data.model.WhatsAppMessageResponse
import com.aarav.geowav.data.datasource.retrofit.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessageRepo {


    fun sendMessage(request: TemplateMessageRequest) {
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
                } else {
                    Log.e("MYTAG", "WhatsApp message failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<WhatsAppMessageResponse>, t: Throwable) {
                Log.e("MYTAG", "WhatsApp API error", t)
            }
        })
    }

}
