package com.aarav.geowav.domain.repository

import com.aarav.geowav.data.place.Place
import kotlinx.coroutines.flow.Flow

interface PlaceRepository {
    suspend fun addPlace(place: Place)
    fun getPlaces() : Flow<List<Place>>
    suspend fun deletePlace(place: Place)


}