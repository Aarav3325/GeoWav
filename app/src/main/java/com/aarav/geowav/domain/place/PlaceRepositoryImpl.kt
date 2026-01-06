package com.aarav.geowav.domain.place

import android.Manifest
import androidx.annotation.RequiresPermission
import com.aarav.geowav.data.geofence.GeofenceHelper
import com.aarav.geowav.data.place.Place
import com.aarav.geowav.data.room.PlacesDAO
import com.aarav.geowav.domain.repository.PlaceRepository
import com.google.android.gms.location.GeofencingClient
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlaceRepositoryImpl @Inject constructor(
    private val placesDAO: PlacesDAO
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

}