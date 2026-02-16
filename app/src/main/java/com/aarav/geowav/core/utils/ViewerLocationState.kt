package com.aarav.geowav.core.utils

import com.aarav.geowav.data.model.LocationUpdates

sealed class ViewerLocationState {
    object Blocked: ViewerLocationState()
    data class NormalSharing(val location: LocationUpdates): ViewerLocationState()
    data class EmergencySharing(val location: LocationUpdates, val endsAt: Long): ViewerLocationState()
}

sealed interface ShareMode {
    data class Emergency(val endsAt: Long): ShareMode
    data object Normal: ShareMode
    data object Blocked: ShareMode
}