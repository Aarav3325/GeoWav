package com.aarav.geowav.data.place

import kotlinx.coroutines.flow.Flow


interface PlaceRepository {
    suspend fun addPlace(place: Place)
    fun getPlaces() : Flow<List<Place>>
    suspend fun deletePlace(place: Place)


}