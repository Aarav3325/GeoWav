package com.aarav.geowav.data.retrofit

import com.aarav.geowav.data.model.TemplateMessageRequest
import com.aarav.geowav.data.model.WhatsAppMessageResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface MessageAPI {

    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer EAAQfKef31ecBP1hChuSxfeGVOqAVOeZBcVWZA28Q6y5gplIN66dHMCI1tRzIYZCG9nCiHuxaKYwemCEilBqXI1hWW4NkLr7zqPZAkJeyIvDZCyMih0JEEBgrqUiof3gR84ShNla6kvZBx4OSJBomJLOqIzYC0rljr1NElAaSSXtGSW2lr8tK5Emtkp0JVjoywWvwZDZD")
    @POST("890118200844088/messages")
    fun postMessage(@Body request: TemplateMessageRequest) : Call<WhatsAppMessageResponse>
}