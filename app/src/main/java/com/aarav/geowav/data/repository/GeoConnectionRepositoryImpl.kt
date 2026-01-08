package com.aarav.geowav.data.repository

import com.aarav.geowav.data.model.GeoConnection
import com.aarav.geowav.data.datasource.room.ConnectionDao
import com.aarav.geowav.domain.repository.GeoConnectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GeoConnectionRepositoryImpl @Inject constructor(
    val connectionDao: ConnectionDao
) : GeoConnectionRepository {
    override suspend fun addNewConnection(connection: GeoConnection) {
        connectionDao.addConnection(connection)
    }

    override suspend fun deleteConnection(connection: GeoConnection) {
        connectionDao.deleteConnection(connection)
    }

    override fun getConnections(): Flow<List<GeoConnection>> {
        return connectionDao.getAllConnections()
    }

}