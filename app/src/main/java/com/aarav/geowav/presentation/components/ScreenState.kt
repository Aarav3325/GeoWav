package com.aarav.geowav.presentation.components

sealed interface AppScreenState<out T> {
    object Loading : AppScreenState<Nothing>
    data class Success<T>(val data: T) : AppScreenState<T>
    object Empty : AppScreenState<Nothing>
    object NoInternet : AppScreenState<Nothing>
    object Timeout : AppScreenState<Nothing>
    object ServerError : AppScreenState<Nothing>
    object UnknownError : AppScreenState<Nothing>
    
    data class PermissionRequired(val type: PermissionType) : AppScreenState<Nothing>
    data class PremiumRequired(val featureName: String) : AppScreenState<Nothing>
    
    object NoSearchResults : AppScreenState<Nothing>
    object FeatureUnavailable : AppScreenState<Nothing>
}

enum class PermissionType {
    LOCATION,
    BACKGROUND_LOCATION,
    NOTIFICATIONS,
    GPS,
    BATTERY_OPTIMIZATION,
    FOREGROUND_SERVICE
}
