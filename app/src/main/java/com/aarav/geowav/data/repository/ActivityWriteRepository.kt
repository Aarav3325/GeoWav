package com.aarav.geowav.data.repository

import com.aarav.geowav.data.model.MovementActivityRecord
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import java.util.Base64

class ActivityWriteRepository(
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun recordMovementActivity(activity: MovementActivityRecord) {
        val actor = checkNotNull(firebaseAuth.currentUser) { "User not logged in" }
        val actorId = actor.uid
        val rootRef = firebaseDatabase.reference

        val stateKey = movementStatePlaceKey(activity.placeName)
        val latestStateSnapshot = Tasks.await(
            rootRef.child("latest_activity_state").child(actorId).child(stateKey).get()
        )
        val latestState = latestStateSnapshot.toLatestMovementActivityState()
        if (shouldSuppressMovementActivity(activity, latestState)) return

        val userSnapshot = Tasks.await(rootRef.child("users").child(actorId).get())
        val actorName = userSnapshot.child("username").getValue(String::class.java)
            ?.takeIf { it.isNotBlank() }
            ?: actor.displayName?.takeIf { it.isNotBlank() }
            ?: "User"
        val actorAvatar = userSnapshot.child("avatar").getValue(String::class.java)
            ?.takeIf { it.isNotBlank() }
            ?: actor.photoUrl?.toString()?.takeIf { it.isNotBlank() }

        val circleSnapshot = Tasks.await(rootRef.child("circle").child(actorId).get())
        val viewerIds = circleSnapshot.children.mapNotNull { member ->
            val isAccepted = member.child("status").getValue(String::class.java) == "accepted"
            member.key?.takeIf { isAccepted }
        }.toSet()

        val activityId = checkNotNull(
            rootRef.child("geofence_activity").child(actorId).push().key
        ) { "Unable to allocate activity id" }
        val feedPayload = activity.toFeedItem(actorId, actorName, actorAvatar).toFirebasePayload()
        val updates = buildMovementActivityUpdates(
            actorId = actorId,
            activityId = activityId,
            stateKey = stateKey,
            viewerIds = viewerIds,
            personalHistoryPayload = activity.toPersonalHistoryPayload(),
            feedPayload = feedPayload,
            latestStatePayload = activity.toLatestStatePayload()
        )
        Tasks.await(rootRef.updateChildren(updates))
    }
}

internal const val MOVEMENT_DUPLICATE_DEBOUNCE_WINDOW_MS = 2 * 60 * 1000L
internal const val MOVEMENT_OPPOSITE_STABILIZATION_WINDOW_MS = 5 * 60 * 1000L

internal data class LatestMovementActivityState(
    val normalizedTransitionType: String,
    val timestamp: Long
)

internal fun shouldSuppressMovementActivity(
    activity: MovementActivityRecord,
    latestState: LatestMovementActivityState?
): Boolean {
    if (latestState == null) return false

    val elapsedMs = activity.timestamp - latestState.timestamp
    if (elapsedMs < 0) return false

    return when {
        latestState.normalizedTransitionType == activity.transition.name ->
            elapsedMs <= MOVEMENT_DUPLICATE_DEBOUNCE_WINDOW_MS

        elapsedMs <= MOVEMENT_OPPOSITE_STABILIZATION_WINDOW_MS ->
            true

        else -> false
    }
}

internal fun movementStatePlaceKey(placeName: String): String {
    val normalizedPlaceName = placeName.trim()
    val keySource = normalizedPlaceName.ifEmpty { placeName.ifEmpty { "unknown_place" } }
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(keySource.toByteArray(Charsets.UTF_8))
}

private fun MovementActivityRecord.toLatestStatePayload(): Map<String, Any> = mapOf(
    "normalizedTransitionType" to transition.name,
    "timestamp" to timestamp
)

private fun DataSnapshot.toLatestMovementActivityState(): LatestMovementActivityState? {
    val normalizedTransitionType = child("normalizedTransitionType")
        .getValue(String::class.java)
        ?.takeIf { it.isNotBlank() }
        ?: return null
    val timestamp = child("timestamp").getValue(Long::class.java) ?: return null

    return LatestMovementActivityState(
        normalizedTransitionType = normalizedTransitionType,
        timestamp = timestamp
    )
}

internal fun buildMovementActivityUpdates(
    actorId: String,
    activityId: String,
    stateKey: String,
    viewerIds: Set<String>,
    personalHistoryPayload: Map<String, Any>,
    feedPayload: Map<String, Any>,
    latestStatePayload: Map<String, Any>
): Map<String, Any?> = buildMap {
    put("latest_activity_state/$actorId/$stateKey", latestStatePayload)
    put("geofence_activity/$actorId/$activityId", personalHistoryPayload)
    (viewerIds + actorId).forEach { viewerId ->
        put("circle_activity/$viewerId/$activityId", feedPayload)
    }
}
