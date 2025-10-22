package com.aarav.geowav.data.datastore

import android.util.Log
import com.aarav.geowav.data.place.Place
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class PlacesDataStore(val placeReference : DatabaseReference){
    fun addPlace(place: Place) {
        placeReference.child("users").child("test123").child("places").child(place.placeId)
            .setValue(place)
            .addOnSuccessListener {

                Log.d("MYTAG", "OK")
            }
            .addOnFailureListener(object : OnFailureListener{
                override fun onFailure(p0: Exception) {
                    TODO("Not yet implemented")
                    Log.e("MYTAG", p0.message.toString())
                }

            })
    }

    fun getPlaces(onResult: (List<Place>) -> Unit) {
        placeReference.child("users").child("test123").child("places")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val places = snapshot.children.mapNotNull { it.getValue(Place::class.java) }
                    onResult(places)
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(emptyList())
                }
            })
    }

    fun removePlace(userId: String, placeId: String) {
        placeReference.child("users").child("test123").child("places").child(placeId)
            .removeValue()
    }
}