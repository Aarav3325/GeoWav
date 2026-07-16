package com.aarav.geowav.data.model

data class ReleaseNote(
    val versionCode: Int,
    val versionName: String,
    val releaseDate: String,
    val title: String,
    val summary: String,
    val features: List<ReleaseFeature>
)

data class ReleaseFeature(
    val title: String,
    val description: String,
    val icon: String
)
