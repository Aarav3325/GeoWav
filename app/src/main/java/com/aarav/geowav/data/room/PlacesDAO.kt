package com.aarav.geowav.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.aarav.geowav.data.place.Place
import kotlinx.coroutines.flow.Flow

@Dao
interface PlacesDAO {

    @Insert
    suspend fun insertPlace(place : Place)

    @Delete
    suspend fun deletePlace(place : Place)

    @Query("SELECT * FROM places")
    fun getAllPlaces() : Flow<List<Place>>
}