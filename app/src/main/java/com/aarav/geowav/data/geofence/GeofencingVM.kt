package com.aarav.geowav.data.geofence

import android.Manifest
import android.app.Application
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.aarav.geowav.data.place.Place
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GeofencingVM @Inject constructor(
    private val geofencingRepo: GeofenceRepositoryImpl
) : ViewModel() {

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun registerPlaces(placeList: List<Place>) {
        geofencingRepo.registerAllPlaces(placeList)
    }
}
