package com.aarav.geowav.data.datasource.retrofit

import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GoogleRoadsRetrofitInstance {

    private const val BASE_URL = "https://roads.googleapis.com/v1/"

    fun getRoadsApi(): RoadsApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RoadsApi::class.java)
    }
}