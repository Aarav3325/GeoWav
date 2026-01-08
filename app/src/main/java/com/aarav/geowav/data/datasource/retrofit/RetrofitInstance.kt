package com.aarav.geowav.data.datasource.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://graph.facebook.com/v22.0/"

    fun getMessagesAPI(): MessageAPI {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MessageAPI::class.java)
    }
}
