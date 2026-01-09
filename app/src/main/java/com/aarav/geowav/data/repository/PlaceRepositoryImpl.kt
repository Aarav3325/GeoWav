package com.aarav.geowav.data.repository

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.datasource.room.PlacesDAO
import com.aarav.geowav.domain.repository.PlaceRepository
import com.aarav.geowav.core.utils.Resource
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Lazy
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class PlaceRepositoryImpl @Inject constructor(
    private val placesDAO: PlacesDAO,
    private val placesClient: Lazy<PlacesClient>
) : PlaceRepository {


    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override suspend fun addPlace(place: Place) {
        placesDAO.insertPlace(place)
    }

    override suspend fun deletePlace(place: Place) {
        placesDAO.deletePlace(place)
    }

    override fun getPlaces(): Flow<List<Place>> {
        return placesDAO.getAllPlaces()
    }

    // Places API Implementation - used to search places and fetch place details
    override suspend fun searchPlaces(
        query: String,
    ): Resource<List<AutocompletePrediction>> {
        return try {
            withTimeout(5_000) { // 5 seconds
                val token = AutocompleteSessionToken.newInstance()
                val client = placesClient.get()

                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .setSessionToken(token)
                    .build()

                val response = client
                    .findAutocompletePredictions(request)
                    .await()

                Resource.Success(response.autocompletePredictions)
            }
        } catch (e: TimeoutCancellationException) {
            Resource.Error(message = "Request timed out. Check your internet connection.")
        }
        catch (e: CancellationException) {
            Log.i("PlacesAPI", e.message.toString())
            return Resource.Error(message = e.message ?: "no connection found")
            throw e
        } catch (e: IOException) {

            Log.i("PlacesAPI", e.message.toString())
            return Resource.Error(message = "No internet connection")
        } catch (e: Exception) {
            Log.i("PlacesAPI", e.message.toString())

            return Resource.Error(message = e.message ?: "Failed to search places")
        }

    }

    // Places API Implementation - used to fetch details of a particular place using palceId
    override suspend fun fetchPlace(
        placeId: String
    ): Resource<com.google.android.libraries.places.api.model.Place> {
        return try {
            val fields = listOf(
                com.google.android.libraries.places.api.model.Place.Field.ID,
                com.google.android.libraries.places.api.model.Place.Field.DISPLAY_NAME,
                com.google.android.libraries.places.api.model.Place.Field.LOCATION,
                com.google.android.libraries.places.api.model.Place.Field.SHORT_FORMATTED_ADDRESS
            )

            val client = placesClient.get()

            val request = FetchPlaceRequest.builder(placeId, fields).build()

            val response = client.fetchPlace(request).await()

            Resource.Success(response.place)
        }
        catch (e: CancellationException) {
            throw e
        }
        catch (e: Exception){
            return Resource.Error(message = e.message ?: "Unable to fetch place details")
        }
    }

}