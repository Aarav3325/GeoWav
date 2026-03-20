package com.aarav.geowav.data.repository

import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.domain.repository.SubscriptionRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class SubscriptionRepositoryImpl
@Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val googleSignInClient: GoogleSignInClient
) : SubscriptionRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeUserPlan(): Flow<UserPlan> {
        return googleSignInClient.getUserIdFlow()
            .distinctUntilChanged()
            .flatMapLatest { uid ->

                if (uid.isBlank()) {
                    return@flatMapLatest flowOf(UserPlan.FREE)
                }

                callbackFlow {
                    val ref = firebaseDatabase.getReference("subscriptions")
                        .child(uid)

                    val listener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val planString = snapshot.child("plan")
                                .getValue(String::class.java)

                            val plan = try {
                                UserPlan.valueOf(planString ?: "FREE")
                            } catch (e: Exception) {
                                UserPlan.FREE
                            }

                            trySend(plan)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            close(error.toException())
                        }
                    }

                    ref.addValueEventListener(listener)

                    awaitClose {
                        ref.removeEventListener(listener)
                    }
                }
            }
    }
}