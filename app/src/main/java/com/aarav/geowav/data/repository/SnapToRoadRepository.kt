package com.aarav.geowav.data.repository

import android.util.Log
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.data.datasource.retrofit.RoadsApi
import com.aarav.geowav.data.model.SnapToRoadResponse
import com.aarav.geowav.data.model.SnappedPoint
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import java.io.IOException

class SnapToRoadRepository(
    val roadsApi: RoadsApi
) {
    suspend fun snapToRoad(
        path: String,
        interpolate: Boolean
    ): Resource<List<SnappedPoint>> {
        return try {

            Log.i("SNAP", "repo call" + path.split(",").size)
            
            val response = roadsApi.snapToRoads(
                path = path,
                interpolate = interpolate
            )

            if (response.isSuccessful) {
                Log.i("SNAP", "repo call success")

                val body = response.body()

                if (body != null) {
                    val error = response.errorBody()?.string()

                    Log.e("SNAP", "Snap API error: $error")

                    Log.e("SNAP", "API ERROR: $error")
                    Log.e("SNAP", "HTTP CODE: ${response.code()}")
                    Resource.Success(body.snappedPoints)
                } else {
                    Resource.Error("Response body is null")
                }

            } else {

                val error = response.errorBody()?.string()
                Log.e("SNAP", "error body: $error")
                Log.i("SNAP", "error body:" + response.errorBody().toString())
                Resource.Error(response.message())
            }

        } catch (t: Throwable) {

            Log.e("SNAP", "Throwable caught: ${t.message}")
            t.printStackTrace()

            Resource.Error(t.message ?: "Unknown error")
        }
    }
}