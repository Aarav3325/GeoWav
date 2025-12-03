package com.aarav.geowav.data.model

    data class GeoAlert(
        val id: String,
        val title: String,
        val subtitle: String,
        val time: String,
        val readableTime: Long,
        val type: String // "enter" / "exit"
    )