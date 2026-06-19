package com.aarav.geowav.data.repository

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.room.withTransaction
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.data.datasource.remote.PlaceRemoteDataSource
import com.aarav.geowav.data.datasource.room.PlaceDatabase
import com.aarav.geowav.data.datasource.room.PlacesDAO
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.domain.repository.PlaceRepository
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
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class PlaceRepositoryImpl @Inject constructor(
    private val remote: PlaceRemoteDataSource,
    private val placesDAO: PlacesDAO,
    private val database: PlaceDatabase,
    private val placesClient: Lazy<PlacesClient>
) : PlaceRepository {


    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override suspend fun addPlace(place: Place) {
        placesDAO.insertPlace(place)

        try {
            remote.uploadPlace(place)

        } catch (e: Exception) {

            Log.w(
                "PlaceRepository",
                "Unable to upload place",
                e
            )
        }
    }

    override suspend fun deletePlace(place: Place) {
        placesDAO.deletePlace(place)

        try {
            remote.deletePlace(place.placeId)
        } catch (e: Exception) {

            Log.w(
                "PlaceRepository",
                "Unable to delete cloud place",
                e
            )
        }
    }

    override suspend fun restorePlaces() {

        try {

            val cloudPlaces = remote.fetchPlaces()

            if (cloudPlaces.isEmpty()) {

                val localPlaces = placesDAO.getAllPlacesOnce()

                localPlaces.forEach {
                    remote.uploadPlace(it)
                }

                return
            }

            database.withTransaction {

                placesDAO.clear()

                placesDAO.insertPlaces(cloudPlaces)

            }

        } catch (e: Exception) {

            Log.e("PlaceRepository", "Failed to restore places", e)
        }
    }

    override suspend fun updatePlace(place: Place) {
        placesDAO.updatePlace(place)

        try {
            remote.updatePlace(place)
        } catch (e: Exception) {

            Log.w(
                "PlaceRepository",
                "Unable to update cloud place",
                e
            )
        }
    }

    override fun getPlaces(): Flow<List<Place>> {
        return placesDAO.getAllPlaces()
    }

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
        } catch (e: CancellationException) {
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Resource.Error(message = e.message ?: "Unable to fetch place details")
        }
    }

    override suspend fun getPlaceById(placeId: String): Place? {
        return placesDAO.getPlaceById(placeId)
    }
}