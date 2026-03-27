package com.aarav.geowav.core.tracking

import com.aarav.geowav.data.model.StayPoint
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

class StayPointTracker {

    private val _finalizedStays = mutableListOf<StayPoint>()

    private var _activeStay: ActiveStay? = null

    val activeQualifiedStay: StayPoint?
        get() {
            val active = _activeStay ?: return null
            val duration = active.lastSeen - active.startedAt
            return if (duration >= StayPoint.STAY_MIN_DURATION_MS) {
                StayPoint(
                    lat = active.anchorLat,
                    lng = active.anchorLng,
                    startedAt = active.startedAt,
                    endedAt = active.lastSeen,
                    durationMillis = duration
                )
            } else null
        }

    fun consumeQualifiedStay(): StayPoint? {
        val qualified = activeQualifiedStay ?: return null

        finalizeActiveIfQualified()
        _activeStay = null

        return qualified
    }

    // Update with a new location
    fun onLocationUpdate(lat: Double, lng: Double, timestamp: Long) {
        val current = _activeStay

        if (current == null) {
            // No active stay, start tracking a potential new one
            // Create an anchor at that location
            _activeStay = ActiveStay(
                anchorLat = lat,
                anchorLng = lng,
                startedAt = timestamp,
                lastSeen = timestamp
            )
            return
        }

        // Calculate distance from anchor to new location
        val distance = SphericalUtil.computeDistanceBetween(
            LatLng(current.anchorLat, current.anchorLng), // anchor
            LatLng(lat, lng) // new position
        )

        if (distance <= StayPoint.STAY_RADIUS_METERS) {
            // Still within radius (30m) - update lastSeen
            _activeStay = current.copy(lastSeen = timestamp)
        } else {
            // Moved outside - finalize if qualified, then start new tracking
            finalizeActiveIfQualified()
            _activeStay = ActiveStay(
                anchorLat = lat,
                anchorLng = lng,
                startedAt = timestamp,
                lastSeen = timestamp
            )
        }
    }

    // Finalization
    fun finalizeAll(): List<StayPoint> {
        finalizeActiveIfQualified()
        _activeStay = null
        return _finalizedStays.toList()
    }

    // Check if active is qualified to be marked as a stay point
    private fun finalizeActiveIfQualified() {
        // Nothing to do if no active stay
        val active = _activeStay ?: return

        val duration = active.lastSeen - active.startedAt

        // Check if the stay is long enough i.e. 10 mins
        if (duration >= StayPoint.STAY_MIN_DURATION_MS) {

            // Add stay point to _finalizedStays
            _finalizedStays.add(
                StayPoint(
                    lat = active.anchorLat,
                    lng = active.anchorLng,
                    startedAt = active.startedAt,
                    endedAt = active.lastSeen,
                    durationMillis = duration
                )
            )
        }
    }

    private data class ActiveStay(
        val anchorLat: Double,
        val anchorLng: Double,
        val startedAt: Long,
        val lastSeen: Long
    )
}
