package com.aarav.geowav.core.utils

import com.aarav.geowav.data.model.LocationUpdates
import com.aarav.geowav.data.model.StayPoint
import com.google.android.gms.maps.model.LatLng

sealed class ViewerLocationState {

    data class NormalSharing(
        val location: LocationUpdates,
        val path: List<LatLng>,
        val stayPoints: List<StayPoint>
    ) : ViewerLocationState()

    data class EmergencySharing(
        val location: LocationUpdates,
        val endsAt: Long,
        val path: List<LatLng>,
        val stayPoints: List<StayPoint>
    ) : ViewerLocationState()

    object Blocked : ViewerLocationState()
}

sealed interface ShareMode {
    data class Emergency(val endsAt: Long): ShareMode
    data object Normal: ShareMode
    data object Blocked: ShareMode
}