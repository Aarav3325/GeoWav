package com.aarav.geowav.data.repository

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.room.withTransaction
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.core.utils.withNetworkTimeout
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class PlaceRepositoryImpl @Inject constructor(
    private val remote: PlaceRemoteDataSource,
    private val placesDAO: PlacesDAO,
    private val database: PlaceDatabase,
    private val placesClient: Lazy<PlacesClient>
) : PlaceRepository {

    private var syncJob: Job? = null

    private var initialSync = true

    override fun startRealtimeSync(
        scope: CoroutineScope
    ) {

        syncJob?.cancel()

        syncJob = scope.launch {

            remote.observePlaces()
                .distinctUntilChanged()
                .collectLatest { remotePlaces ->

                    val localPlaces = placesDAO
                        .getAllPlacesOnce()
                        .sortedBy { it.placeId }

                    val remoteSorted = remotePlaces
                        .sortedBy { it.placeId }

                    if (localPlaces == remoteSorted) {
                        return@collectLatest
                    }

                    replaceRoom(remotePlaces)
                }
        }
    }

    override fun stopRealtimeSync() {
        syncJob?.cancel()
        syncJob = null
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override suspend fun addPlace(place: Place) {
        placesDAO.insertPlace(place)

        try {
            remote.uploadPlace(place)

        } catch (e: Exception) {

            Log.w(
                "PlaceRepository", "Unable to upload place", e
            )
        }
    }

    override suspend fun deletePlace(place: Place) {
        placesDAO.deletePlace(place)

        try {
            remote.deletePlace(place.placeId)
        } catch (e: Exception) {

            Log.w(
                "PlaceRepository", "Unable to delete cloud place", e
            )
        }
    }

    override suspend fun updatePlace(place: Place) {
        placesDAO.updatePlace(place)

        try {
            remote.updatePlace(place)
        } catch (e: Exception) {

            Log.w(
                "PlaceRepository", "Unable to update cloud place", e
            )
        }
    }

    override fun getPlaces(): Flow<List<Place>> {
        return placesDAO.getAllPlaces()
    }

    override suspend fun searchPlaces(
        query: String,
    ): Resource<List<AutocompletePrediction>> {
        return withNetworkTimeout {
                val token = AutocompleteSessionToken.newInstance()
                val client = placesClient.get()

                val request = FindAutocompletePredictionsRequest.builder().setQuery(query)
                    .setSessionToken(token).build()

                val response = client.findAutocompletePredictions(request).await()

                response.autocompletePredictions
        }
    }

    override suspend fun fetchPlace(
        placeId: String
    ): Resource<com.google.android.libraries.places.api.model.Place> {
        return withNetworkTimeout {
            val fields = listOf(
                com.google.android.libraries.places.api.model.Place.Field.ID,
                com.google.android.libraries.places.api.model.Place.Field.DISPLAY_NAME,
                com.google.android.libraries.places.api.model.Place.Field.LOCATION,
                com.google.android.libraries.places.api.model.Place.Field.SHORT_FORMATTED_ADDRESS
            )

            val client = placesClient.get()

            val request = FetchPlaceRequest.builder(placeId, fields).build()

            val response = client.fetchPlace(request).await()

            response.place
        }
    }

    override suspend fun getPlaceById(placeId: String): Place? {
        return placesDAO.getPlaceById(placeId)
    }

    private suspend fun replaceRoom(
        places: List<Place>
    ) {
        database.withTransaction {

            placesDAO.clear()

            placesDAO.insertPlaces(places)
        }
    }

    override suspend fun migratePlacesIfNeeded() {

        val localPlaces = placesDAO.getAllPlacesOnce()

        if (localPlaces.isEmpty()) return

        val remotePlaces = try {
            remote.fetchPlaces()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("PlaceRepository", "Unable to fetch cloud places", e)
            return
        }

        if (remotePlaces.isNotEmpty()) return

        remote.uploadPlaces(localPlaces)
    }

}
