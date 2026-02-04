package com.aarav.geowav.data.datasource.retrofit

import com.aarav.geowav.data.model.TemplateMessageRequest
import com.aarav.geowav.data.model.WhatsAppMessageResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface MessageAPI {

    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer EAAQfKef31ecBQiyXW4NoIsGvQHKEzvts449dvYVHugjbmuu1Xn7XgRWKYZCzve1fN6VpBcFBLEsDqugWPL66RHjeIdqSDAfyItYhZB6ePZAcN6RrsFTxABfIP5JJqpHC9xWWFjWqglKLg8ZCBmF9ZAImR518EXxG4ZASC8RGIfq8j3szlIJ4jE7lvMlWFPoN6fFQLaVcfiRvPDyrGDpH0zOyElaChpCVmc4yWxvaSyl9EVHC9WJa3G")
    @POST("890118200844088/messages")

    fun postMessage(@Body request: TemplateMessageRequest) : Call<WhatsAppMessageResponse>
}