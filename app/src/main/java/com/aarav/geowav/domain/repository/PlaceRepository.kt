package com.aarav.geowav.domain.repository

import com.aarav.geowav.data.place.Place
import com.aarav.geowav.domain.utils.Resource
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlinx.coroutines.flow.Flow

interface PlaceRepository {
    suspend fun addPlace(place: Place)
    fun getPlaces(): Flow<List<Place>>
    suspend fun deletePlace(place: Place)

    suspend fun fetchPlace(placeId: String): Resource<com.google.android.libraries.places.api.model.Place>
    suspend fun searchPlaces(
        query: String,
    ): Resource<List<AutocompletePrediction>>
}