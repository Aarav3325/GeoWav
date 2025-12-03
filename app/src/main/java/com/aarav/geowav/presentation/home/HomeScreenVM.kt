package com.aarav.geowav.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.data.geofence.ActivityFilter
import com.aarav.geowav.data.model.GeoAlert
import com.aarav.geowav.data.model.GeoConnection
import com.aarav.geowav.data.model.User
import com.aarav.geowav.data.repository.GeoActivityRepositoryImpl
import com.aarav.geowav.data.repository.GeoConnectionRepositoryImpl
import com.aarav.geowav.domain.place.PlaceRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeScreenVM @Inject constructor(val connectionRepository: GeoConnectionRepositoryImpl,
    val placeRepository: PlaceRepositoryImpl,
    val geoActivityRepositoryImpl: GeoActivityRepositoryImpl): ViewModel() {
    fun addConnection(connection: GeoConnection){
        connectionRepository.addNewConnection(connection)
    }

    fun deleteConnection(connection: GeoConnection){
        connectionRepository.deleteConnection(connection)
    }

    val allConnections = connectionRepository.getConnections().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
        )


    val allPlaces = placeRepository.getPlaces().stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    val alerts = geoActivityRepositoryImpl
        .observeAlerts(ActivityFilter.Today)
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

}