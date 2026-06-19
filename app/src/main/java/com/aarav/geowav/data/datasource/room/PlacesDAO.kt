package com.aarav.geowav.data.datasource.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aarav.geowav.data.model.Place
import kotlinx.coroutines.flow.Flow

@Dao
interface PlacesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: Place)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaces(places: List<Place>)

    @Delete
    suspend fun deletePlace(place : Place)

    @Query("SELECT * FROM places")
    fun getAllPlaces() : Flow<List<Place>>

    @Query("SELECT * FROM places")
    suspend fun getAllPlacesOnce(): List<Place>

    @Query("DELETE FROM places")
    suspend fun clear()

    @androidx.room.Update
    suspend fun updatePlace(place: Place)

    @Query("SELECT * FROM places WHERE placeId = :placeId LIMIT 1")
    suspend fun getPlaceById(placeId: String): Place?
}