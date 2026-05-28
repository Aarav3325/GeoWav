package com.aarav.geowav.data.repository

import com.aarav.geowav.core.utils.ActivityFilter
import com.aarav.geowav.core.utils.rangeForFilter
import com.aarav.geowav.data.model.ActivityTransition
import com.aarav.geowav.data.model.CircleActivityItem
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CircleActivityFeedRepository @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth
) {
    companion object {
        const val ACTIVITY_PAGE_SIZE = 30
    }

    fun observeRecentActivity(limit: Int = 5): Flow<List<CircleActivityItem>> = callbackFlow {
        val query = activityRef()
            .orderByChild("timestamp")
            .limitToLast(limit)

        observeQuery(query)
    }

    fun observeActivityPage(
        filter: ActivityFilter,
        limit: Int = ACTIVITY_PAGE_SIZE
    ): Flow<List<CircleActivityItem>> = callbackFlow {
        val timeRange = rangeForFilter(filter)
        val query = activityRef()
            .orderByChild("timestamp")
            .startAt(timeRange.startMillis.toDouble())
            .endAt(timeRange.endMillis.toDouble())
            .limitToLast(limit)

        observeQuery(query)
    }

    suspend fun loadOlderActivityPage(
        filter: ActivityFilter,
        olderThanTimestamp: Long,
        limit: Int = ACTIVITY_PAGE_SIZE
    ): List<CircleActivityItem>  {
        val timeRange = rangeForFilter(filter)

        val cursorEndMillis = minOf(
            olderThanTimestamp - 1,
            timeRange.endMillis
        )

        if (cursorEndMillis < timeRange.startMillis) {
            return emptyList()
        }

        val query = activityRef()
            .orderByChild("timestamp")
            .startAt(timeRange.startMillis.toDouble())
            .endAt(cursorEndMillis.toDouble())
            .limitToLast(limit)

        return query.get().await().toActivityItems()
    }

    private fun activityRef() = firebaseDatabase.getReference("circle_activity")
        .child(
            firebaseAuth.currentUser?.uid
                ?: throw IllegalStateException("User not logged in")
        )

    private suspend fun kotlinx.coroutines.channels.ProducerScope<List<CircleActivityItem>>.observeQuery(
        query: Query
    ) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.toActivityItems())
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    private fun DataSnapshot.toActivityItems(): List<CircleActivityItem> {
        return children.mapNotNull { child ->
            child.getValue(CircleActivityItem::class.java)?.takeIf { item ->
                item.actorId.isNotBlank() &&
                        item.actorName.isNotBlank() &&
                        item.placeName.isNotBlank() &&
                        item.timestamp > 0L &&
                        ActivityTransition.fromRaw(item.normalizedTransitionType) != null
            }
        }.sortedByDescending { it.timestamp }
    }
}
