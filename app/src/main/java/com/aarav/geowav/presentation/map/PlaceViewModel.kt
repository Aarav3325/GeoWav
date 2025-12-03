package com.aarav.geowav.presentation.map

import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.data.place.Place
import com.aarav.geowav.domain.repository.PlaceRepository
import com.aarav.geowav.domain.place.PlaceRepositoryImpl
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PlaceViewModel @Inject constructor(val placeRepository: PlaceRepositoryImpl) : ViewModel() {
    val allPlaces: StateFlow<List<Place>> = placeRepository.getPlaces()
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

    fun addPlace(place: Place) {
        viewModelScope.launch {
            placeRepository.addPlace(place)
        }
    }

    fun deletePlace(place: Place) {
        viewModelScope.launch {
            placeRepository.deletePlace(place)
        }
    }

    fun searchPlaces(context: Context, query: String, onResult: (List<AutocompletePrediction>) -> Unit) {
        val token = AutocompleteSessionToken.newInstance()

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setSessionToken(token)
            .build()

        val placesClient = Places.createClient(context)

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                onResult(response.autocompletePredictions)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                onResult(emptyList())
            }
    }

    fun fetchPlace(placeId : String, context: Context,
                   onPlaceSelected: (com.google.android.libraries.places.api.model.Place) -> Unit){
        //val placeId = placeId
        val fields = listOf(
            com.google.android.libraries.places.api.model.Place.Field.ID,
            com.google.android.libraries.places.api.model.Place.Field.DISPLAY_NAME, com.google.android.libraries.places.api.model.Place.Field.LOCATION,
            com.google.android.libraries.places.api.model.Place.Field.SHORT_FORMATTED_ADDRESS)
        val placesClient = Places.createClient(context)
        val request = FetchPlaceRequest.builder(placeId, fields).build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                onPlaceSelected(response.place)
            }


    }

    fun getFormattedDate(): String {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return formatter.format(Date())
    }




}