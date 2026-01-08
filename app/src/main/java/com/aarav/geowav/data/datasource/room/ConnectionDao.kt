package com.aarav.geowav.data.datasource.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.aarav.geowav.data.model.GeoConnection
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionDao {
    @Insert
    suspend fun addConnection(geoConnection: GeoConnection)

    @Delete
    suspend fun deleteConnection(geoConnection: GeoConnection)

    @Query("SELECT * FROM geo_connections")
    fun getAllConnections(): Flow<List<GeoConnection>>
}