package com.aarav.geowav.data.repository

import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.core.utils.rangeForFilter
import com.aarav.geowav.data.model.ActivityTransition
import com.aarav.geowav.data.model.CircleActivityItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class CircleActivityFeedRepository @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth
) {
    fun observeRecentActivity(limit: Int = 5): Flow<List<CircleActivityItem>> = callbackFlow {
        val query = activityRef()
            .orderByChild("timestamp")
            .limitToLast(limit)

        observeQuery(query)
    }

    fun observeActivity(filter: ActivityFilter): Flow<List<CircleActivityItem>> = callbackFlow {
        val timeRange = rangeForFilter(filter)
        val query = activityRef()
            .orderByChild("timestamp")
            .startAt(timeRange.startMillis.toDouble())
            .endAt(timeRange.endMillis.toDouble())

        observeQuery(query)
    }

    private fun activityRef() = firebaseDatabase.getReference("circle_activity")
        .child(
            firebaseAuth.currentUser?.uid
                ?: throw IllegalStateException("User not logged in")
        )

    private suspend fun kotlinx.coroutines.channels.ProducerScope<List<CircleActivityItem>>.observeQuery(
        query: com.google.firebase.database.Query
    ) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val activities = snapshot.children.mapNotNull { child ->
                    child.getValue(CircleActivityItem::class.java)?.takeIf { item ->
                        item.actorId.isNotBlank() &&
                                item.actorName.isNotBlank() &&
                                item.placeName.isNotBlank() &&
                                item.timestamp > 0L &&
                                ActivityTransition.fromRaw(item.normalizedTransitionType) != null
                    }
                }.sortedByDescending { it.timestamp }

                trySend(activities)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }
}
