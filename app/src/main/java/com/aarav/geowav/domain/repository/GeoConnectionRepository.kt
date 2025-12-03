package com.aarav.geowav.domain.repository

import com.aarav.geowav.data.model.GeoConnection
import kotlinx.coroutines.flow.Flow

interface GeoConnectionRepository {
    fun addNewConnection(connection: GeoConnection)
    fun deleteConnection(connection: GeoConnection)
    fun getConnections(): Flow<List<GeoConnection>>
}