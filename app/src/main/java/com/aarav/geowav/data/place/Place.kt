package com.aarav.geowav.data.place

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places")
data class Place(
    @ColumnInfo(name = "placeId")
    @PrimaryKey(autoGenerate = false)
    val placeId: String = "",

    @ColumnInfo(name = "customName")
    val customName: String = "",

    @ColumnInfo(name = "placeName")
    val placeName: String = "",

    @ColumnInfo(name = "lat")
    val latitude: Double = 0.0,

    @ColumnInfo(name = "lng")
    val longitude: Double = 0.0,

    @ColumnInfo(name = "address")
    val address: String?,

    @ColumnInfo(name = "radius")
    val radius: Float = 200f,

    @ColumnInfo(name = "triggerType")
    val triggerType: String = "ENTER_EXIT",

    @ColumnInfo(name = "addedOn")
    val addedOn: String = "",
)
