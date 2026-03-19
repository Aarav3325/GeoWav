package com.aarav.geowav.data.repository

import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.domain.repository.SubscriptionRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class SubscriptionRepositoryImpl
@Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val googleSignInClient: GoogleSignInClient
) : SubscriptionRepository {

    override fun observeUserPlan(): Flow<UserPlan> = callbackFlow {
        val uid = googleSignInClient.getUserId()
        if (uid.isBlank()) {
            trySend(UserPlan.FREE)
            return@callbackFlow
        }

        val ref = firebaseDatabase.getReference("subscriptions")
            .child(uid)

        val listener = object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val planString = p0.child("plan").getValue(String::class.java)

                val plan = try {
                    UserPlan.valueOf(planString ?: "FREE")
                } catch (e: Exception) {
                    UserPlan.FREE
                }

                trySend(plan)
            }

            override fun onCancelled(p0: DatabaseError) {
                close(p0.toException())
            }

        }

        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
        }
    }
}