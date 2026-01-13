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
        "Authorization: Bearer EAAQfKef31ecBQa2tgr6CI0dOTMqrybR5ZBQOzbcXAwxzVEC01ZAHZALAedYH9K9lGBSvkGviYWqWRpzP3l0XHnDjmbvHTCImpWRuFA3M8FgHN5kESvgvu5YefZBK9jwW8NArTK0XaQADVlMplrTfnZBp53ZA1GclXjRNgyuwaZCIJob5zzHFpJ0ZABi3Qs1xMNZCStWGGmaEV3ejharrTqgxFHZCrNJIWEBpjuqTezX0KO")
    @POST("890118200844088/messages")

    fun postMessage(@Body request: TemplateMessageRequest) : Call<WhatsAppMessageResponse>
}