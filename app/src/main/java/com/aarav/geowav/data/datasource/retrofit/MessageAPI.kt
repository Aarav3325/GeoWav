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
        "Authorization: Bearer EAAMtbQLe21UBQsZCS9bhCqSovqIO74ZBcIC3wcck42H2l53HhfRZBa54KsnMdnh5Fsv1aNcTcSJa6bNryMH0aZBv4IwI7OPfA0uSJQjaiFuZAun34qFNKqf400ZCpv1LqZCFiaHFVkea8OOQvetHSxSiwV89J1aBAeZAZBYue0fZAqZAjFReVBhYePEcurPRrFF9PtctVHzquYzWCZBbJd4K8dFS4S88p4RznPNtliYeSDj19ZCXcuDPAhL5ZBihG3ZByOUSjr9SSQt66eQxCE7qK1WA9nSp6ppWW5FoMSOJIZCMlOEZD")
    @POST("886176277923495/messages")

    fun postMessage(@Body request: TemplateMessageRequest) : Call<WhatsAppMessageResponse>
}