package com.aarav.geowav.data.repository

import android.util.Log
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.core.utils.toNetworkResource
import com.aarav.geowav.core.utils.withNetworkTimeout
import com.aarav.geowav.data.datasource.retrofit.RoadsApi
import com.aarav.geowav.data.model.SnappedPoint
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

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
        sessionCache[sessionId]?.let {
            Log.i("SNAP", "cache hit")
            return Resource.Success(it)
        }

        return try {
            val snapped = mutableListOf<SnappedPoint>()

            val chunks = path.chunked(100)

            Log.i("SNAP", "repo call" + chunks.size)

            for ((index, chunk) in chunks.withIndex()) {

                val pathString = chunk.joinToString("|") {
                    "${it.latitude},${it.longitude}"
                }

                val responseResult = withNetworkTimeout {
                    roadsApi.snapToRoads(
                        path = pathString,
                        interpolate = interpolate
                    )
                }

                val response = when (responseResult) {
                    is Resource.Success -> responseResult.data
                    is Resource.NoInternet -> return Resource.NoInternet(responseResult.message ?: "No internet connection")
                    is Resource.Timeout -> return Resource.Timeout(responseResult.message ?: "Taking longer than expected")
                    is Resource.ServerError -> return Resource.ServerError(responseResult.message ?: "Server error")
                    is Resource.UnknownError -> return Resource.UnknownError(responseResult.message ?: "Unable to snap route")
                    is Resource.Error -> return Resource.UnknownError(responseResult.message ?: "Unable to snap route")
                    is Resource.Loading -> return Resource.UnknownError("Unable to snap route")
                } ?: return Resource.UnknownError("Unable to snap route")

                if (!response.isSuccessful) {

                    val error = response.errorBody()?.string()

                    Log.e("SNAP", "Snap API error: $error")

                    Log.e("SNAP", "API ERROR: $error")
                    Log.e("SNAP", "HTTP CODE: ${response.code()}")

                    return Resource.ServerError(response.message())
                }


                val data = response.body()?.snappedPoints ?: emptyList()

                if(data.isEmpty()) return Resource.UnknownError("This session does not include enough route data")

                snapped.addAll(
                    if (index == 0) data
                    else data.drop(1) // remove overlap
                )
            }


            sessionCache[sessionId] = snapped
            Log.i("SNAP", "cached" + sessionCache.size)

            Resource.Success(snapped)

        } catch (t: CancellationException) {
            throw t
        } catch (t: Throwable) {

            Log.e("SNAP", "Throwable caught: ${t.message}")
            t.printStackTrace()

            t.toNetworkResource("Unable to snap route")
        }
    }
}
