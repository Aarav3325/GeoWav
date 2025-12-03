package com.aarav.geowav.domain.place

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import com.aarav.geowav.data.geofence.GeofenceHelper
import com.aarav.geowav.data.place.Place
import com.aarav.geowav.domain.repository.PlaceRepository
import com.aarav.geowav.data.room.PlacesDAO
import com.google.android.gms.location.GeofencingClient
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlaceRepositoryImpl @Inject constructor(
    private val placesDAO: PlacesDAO,
    private val geofencingClient: GeofencingClient,
    private val geofenceHelper: GeofenceHelper
) : PlaceRepository {


    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override suspend fun addPlace(place: Place) {
        placesDAO.insertPlace(place)

        // Register geofence immediately
//        val geofence = geofenceHelper.createGeofence(
//            place.placeId,
//            place.latitude,
//            place.longitude,
//            place.radius
//        )
//        val request = geofenceHelper.createGeofencingRequest(geofence)
//        val pendingIntent = geofenceHelper.getGeofencePendingIntent()
//        geofencingClient.addGeofences(request, pendingIntent)
//            .addOnSuccessListener { Log.d("MYTAG", "${place.placeId} geofence added") }
//            .addOnFailureListener { Log.e("MYTAG", "Failed to add geofence", it) }
    }

    override suspend fun deletePlace(place: Place) {
        //placesDataStore.addPlace(place)
        placesDAO.deletePlace(place)
    }

    override fun getPlaces() : Flow<List<Place>> {
//        placesDataStore.getPlaces {
//            onResult(it)
//        }

        return placesDAO.getAllPlaces()
    }

}