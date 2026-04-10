package com.aarav.geowav.data.datasource.retrofit

import com.aarav.geowav.BuildConfig
import com.aarav.geowav.data.model.TemplateMessageRequest
import com.aarav.geowav.data.model.WhatsAppMessageResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface MessageAPI {

    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer ${BuildConfig.META_ACCESS_TOEKN}")
    @POST("886176277923495/messages")

    fun postMessage(@Body request: TemplateMessageRequest) : Call<WhatsAppMessageResponse>
}