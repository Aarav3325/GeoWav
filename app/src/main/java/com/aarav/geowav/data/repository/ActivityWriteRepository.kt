package com.aarav.geowav.data.repository

import com.aarav.geowav.data.model.MovementActivityRecord
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ActivityWriteRepository(
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun recordMovementActivity(activity: MovementActivityRecord) {
        val actor = checkNotNull(firebaseAuth.currentUser) { "User not logged in" }
        val actorId = actor.uid
        val rootRef = firebaseDatabase.reference

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
            viewerIds = viewerIds,
            personalHistoryPayload = activity.toPersonalHistoryPayload(),
            feedPayload = feedPayload
        )
        Tasks.await(rootRef.updateChildren(updates))
    }
}

internal fun buildMovementActivityUpdates(
    actorId: String,
    activityId: String,
    viewerIds: Set<String>,
    personalHistoryPayload: Map<String, Any>,
    feedPayload: Map<String, Any>
): Map<String, Any?> = buildMap {
    put("geofence_activity/$actorId/$activityId", personalHistoryPayload)
    (viewerIds + actorId).forEach { viewerId ->
        put("circle_activity/$viewerId/$activityId", feedPayload)
    }
}
