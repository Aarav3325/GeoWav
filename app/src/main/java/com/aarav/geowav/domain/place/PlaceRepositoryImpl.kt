package com.aarav.geowav.domain.place

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.aarav.geowav.data.place.Place
import com.aarav.geowav.data.room.PlacesDAO
import com.aarav.geowav.domain.repository.PlaceRepository
import com.aarav.geowav.domain.utils.Resource
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Lazy
import jakarta.inject.Provider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
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

    override suspend fun searchPlaces(
        query: String,
    ): Resource<List<AutocompletePrediction>> {
        return try {
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

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Resource.Error(message = e.message ?: "Failed to search places")
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
        }
        catch (e: CancellationException) {
            throw e
        }
        catch (e: Exception){
            return Resource.Error(message = e.message ?: "Unable to fetch place details")
        }
    }

    fun getFormattedDate(): String {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return formatter.format(Date())
    }

}

