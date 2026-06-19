package com.aarav.geowav.data.datasource.remote

import com.aarav.geowav.data.model.Place
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PlaceRemoteDataSource @Inject constructor(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) {
    private fun placeRef() =
        database.reference
            .child("users")
            .child(auth.currentUser!!.uid)
            .child("places")

    suspend fun uploadPlace(place: Place) {

        placeRef()
            .child(place.placeId)
            .setValue(place)
            .await()
    }

    suspend fun updatePlace(place: Place) {

        placeRef()
            .child(place.placeId)
            .setValue(place)
            .await()
    }

    suspend fun deletePlace(placeId: String) {

        placeRef()
            .child(placeId)
            .removeValue()
            .await()
    }


    fun observePlaces(): Flow<List<Place>> = callbackFlow {

        val listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val places = snapshot.children.mapNotNull {
                    it.getValue(Place::class.java)
                }

                trySend(places)
            }

            override fun onCancelled(error: DatabaseError) {

                close(error.toException())

            }
        }

        placeRef().addValueEventListener(listener)

        awaitClose {
            placeRef().removeEventListener(listener)

        }
    }
}