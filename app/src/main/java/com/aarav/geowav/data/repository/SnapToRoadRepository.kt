package com.aarav.geowav.data.repository

import android.util.Log
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.data.datasource.retrofit.RoadsApi
import com.aarav.geowav.data.model.SnapToRoadResponse
import com.aarav.geowav.data.model.SnappedPoint
import com.google.gson.Gson
import java.io.IOException

class SnapToRoadRepository(
    val roadsApi: RoadsApi
) {
    suspend fun snapToRoad(
        path: String,
        interpolate: Boolean,
        apiKey: String
    ): Resource<List<SnappedPoint>> {
        return try {

            Log.i("SNAP", "repo call")
            val response = roadsApi.snapToRoads(
                path = path,
                interpolate = interpolate,
                apiKey = apiKey
            )

            if (response.isSuccessful) {
                Log.i("SNAP", "repo call success")

                val body = response.body()

                if (body != null) {
                    Log.i("SNAP", "body" + body.snappedPoints.toString())
                    Resource.Success(body.snappedPoints)
                } else {
                    Resource.Error("Response body is null")
                }

            } else {

                Log.i("SNAP", "body" + response.errorBody())
                Resource.Error(response.message())
            }

        }
        catch (e: IOException) {
            Resource.Error("Network error")
        }
        catch (e: Exception) {
            Log.i("SNAP", "repo error:" + e.message)
            Resource.Error("Server error" + e.message)
        }
    }
}