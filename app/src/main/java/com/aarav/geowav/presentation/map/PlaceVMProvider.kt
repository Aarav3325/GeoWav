package com.aarav.geowav.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aarav.geowav.data.place.PlaceRepository
import com.aarav.geowav.domain.place.PlaceRepositoryImpl

class PlaceVMProvider(val placeRepository: PlaceRepositoryImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(PlaceViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return PlaceViewModel(placeRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel")
    }
}