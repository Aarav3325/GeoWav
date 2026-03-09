package com.aarav.geowav.data.repository

import android.util.Log
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.data.datasource.retrofit.RoadsApi
import com.aarav.geowav.data.model.SnappedPoint
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SnapToRoadRepository @Inject constructor(
    val roadsApi: RoadsApi
) {


    private val sessionCache = mutableMapOf<String, List<SnappedPoint>>()

    suspend fun snapToRoad(
        sessionId: String,
        path: List<LatLng>,
        interpolate: Boolean
    ): Resource<List<SnappedPoint>> {
        return try {

            sessionCache[sessionId]?.let {
                Log.i("SNAP", "cache hit")
                return Resource.Success(it)
            }

            val snapped = mutableListOf<SnappedPoint>()

            val chunks = path.chunked(100)

            Log.i("SNAP", "repo call" + chunks.size)

            for ((index, chunk) in chunks.withIndex()) {

                val pathString = chunk.joinToString("|") {
                    "${it.latitude},${it.longitude}"
                }

                val response = roadsApi.snapToRoads(
                    path = pathString,
                    interpolate = interpolate
                )

                if (!response.isSuccessful) {

                    val error = response.errorBody()?.string()

                    Log.e("SNAP", "Snap API error: $error")

                    Log.e("SNAP", "API ERROR: $error")
                    Log.e("SNAP", "HTTP CODE: ${response.code()}")

                    return Resource.Error(response.message())
                }


                val data = response.body()?.snappedPoints ?: emptyList()

                snapped.addAll(
                    if (index == 0) data
                    else data.drop(1) // remove overlap
                )
            }


            sessionCache[sessionId] = snapped
            Log.i("SNAP", "cached" + sessionCache.size)

            Resource.Success(snapped)

        } catch (t: Throwable) {

            Log.e("SNAP", "Throwable caught: ${t.message}")
            t.printStackTrace()

            Resource.Error(t.message ?: "Unknown error")
        }
    }
}