package com.aarav.geowav.data.model

enum class ActivityTransition {
    ARRIVED,
    LEFT;

    companion object {
        fun fromRaw(value: String?): ActivityTransition? {
            return when (value?.trim()?.uppercase()) {
                "ARRIVED", "ENTER", "REACHED" -> ARRIVED
                "LEFT", "EXIT" -> LEFT
                else -> null
            }
        }
    }
}
