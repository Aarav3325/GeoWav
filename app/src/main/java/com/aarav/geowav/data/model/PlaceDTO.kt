package com.aarav.geowav.data.model

data class PlaceDto(
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Float = 0f,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)