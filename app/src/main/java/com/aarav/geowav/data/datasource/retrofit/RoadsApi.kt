package com.aarav.geowav.data.datasource.retrofit

import com.aarav.geowav.data.model.SnapToRoadResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RoadsApi {

    @GET("snapToRoads")
    suspend fun snapToRoads(
        @Query("interpolate") interpolate: Boolean,
        @Query("path") path: String,
        @Query("key") apiKey: String
    ): Response<SnapToRoadResponse>

}