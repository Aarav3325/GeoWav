package com.aarav.geowav.core.tracking

import com.aarav.geowav.data.model.StayPoint
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

/**
 * In-memory tracker that detects stay points for a single user.
 *
 * A stay point is created when the user remains within
 * [StayPoint.STAY_RADIUS_METERS] of an anchor point for at least
 * [StayPoint.STAY_MIN_DURATION_MS].
 */
class StayPointTracker {

    /** Finalized stay points that exceeded the minimum duration. */
    private val _finalizedStays = mutableListOf<StayPoint>()

    /** Currently active (potential) stay. 'null' when the user is moving. */
    private var _activeStay: ActiveStay? = null


    /** All finalized stay points so far. */
    val finalizedStays: List<StayPoint> get() = _finalizedStays.toList()

    /**
     * The currently active stay if it has already exceeded the
     * minimum duration threshold, otherwise 'null'.
     *
     * Use this for live UI — it returns a [StayPoint] snapshot whose
     * `endedAt` and `durationMillis` reflect the latest update.
     */
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

    /**
     * Feed a new location update. Call this each time a valid
     * location comes in from the live sharing flow.
     */
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

    /**
     * Call when the session ends (Blocked state) to finalize any
     * in-progress stay before persisting to Firebase.
     *
     * @return all stay points (finalized + the last active one if qualified)
     */
    fun finalizeAll(): List<StayPoint> {
        finalizeActiveIfQualified()
        _activeStay = null
        return _finalizedStays.toList()
    }

    /**
     * Clears all state. Call when a user's session is fully done
     * and the tracker instance is being discarded / recycled.
     */
    fun reset() {
        _finalizedStays.clear()
        _activeStay = null
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

    /**
     * Internal mutable state representing a potential stay in progress.
     */
    private data class ActiveStay(
        val anchorLat: Double,
        val anchorLng: Double,
        val startedAt: Long,
        val lastSeen: Long
    )
}
