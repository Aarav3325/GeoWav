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
        "Authorization: Bearer EAAMtbQLe21UBQpA9VbBOKmcptDtZAMp6O8HSpkNZB4sgXDCPx9jSYvPeGtZCCaL3j5wlfdVZAla66gjWjIfsZB71dLEZAHfwZArOoBiXq3XsvJMbzp7BesCKQGiQda5ssBl7yvfhT1pzZA1gVjoOgfaZBdGXZCdddqDKBlwX1fBxLxjgLGWgtZCJZC5ofv8UJfitY4bxIp1TLQRs0pNrR89GK0KQsYtsR7ppHaZAY5HCyWswalNjst6jZAKoqXJxnmHSdNQKGf3wDRPu1N3nSBQ8yyhO2ECU28RiptRZAVaj23ZBwAZDZD")
    @POST("886176277923495/messages")

    fun postMessage(@Body request: TemplateMessageRequest) : Call<WhatsAppMessageResponse>
}