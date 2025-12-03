package com.aarav.geowav.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geo_connections")
data class GeoConnection(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val phoneNumber: String? = null,
    val name: String? = null
)

data class FirebaseGeoConnection(
    val id: String? = null,
    val phoneNumber: String? = null,
    val name: String? = null
)
