package com.aarav.geowav.data.model

import com.google.android.gms.maps.model.LatLng

data class SnapToRoadResponse(
    val snappedPoints: List<SnappedPoint> = emptyList(),
    val warningMessage: String
)

data class SnappedPoint(
    val location: LatLng,
    val placeId: String
)