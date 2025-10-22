package com.aarav.geowav.presentation.map

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.data.location.LocationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    @Inject
    lateinit var locationManager : LocationManager

    private var _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

//    fun fetchUserLocation(){
//        viewModelScope.launch {
//            val loc = locationManager.getLastKnownLocation()
//            loc?.let {
//                _currentLocation.value = Pair(it.latitude, it.longitude)
//            }
//        }
//    }

    fun startLocationUpdates() {
        viewModelScope.launch {
            locationManager.getLocationUpdates().collect {
                _currentLocation.value = it
            }
        }
    }
}