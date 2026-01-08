package com.aarav.geowav.data.model

@androidx.room.Entity(tableName = "places")
data class Place(
    @androidx.room.ColumnInfo(name = "placeId")
    @androidx.room.PrimaryKey(autoGenerate = false)
    val placeId: String = "",

    @androidx.room.ColumnInfo(name = "customName")
    val customName: String = "",

    @androidx.room.ColumnInfo(name = "placeName")
    val placeName: String = "",

    @androidx.room.ColumnInfo(name = "lat")
    val latitude: Double = 0.0,

    @androidx.room.ColumnInfo(name = "lng")
    val longitude: Double = 0.0,

    @androidx.room.ColumnInfo(name = "address")
    val address: String?,

    @androidx.room.ColumnInfo(name = "radius")
    val radius: Float = 200f,

    @androidx.room.ColumnInfo(name = "triggerType")
    val triggerType: String = "ENTER_EXIT",

    @androidx.room.ColumnInfo(name = "addedOn")
    val addedOn: String = "",
)