package com.aarav.geowav.data.repository

import android.util.Log
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.datasource.revenuecat.RevenueCatDataSource
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.data.model.UserSubscription
import com.aarav.geowav.domain.repository.SubscriptionRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.revenuecat.purchases.Package
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
    private val revenueCatDataSource: RevenueCatDataSource,
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
                            val forcedPlan = snapshot.child("forcedPlan")
                                .getValue(String::class.java)
                            val overrideEnabled = snapshot.child("overrideEnabled")
                                .getValue(Boolean::class.java) ?: false

                            val resolvedPlanString = if (overrideEnabled && forcedPlan != null) {
                                forcedPlan
                            } else {
                                planString ?: "FREE"
                            }

                            val plan = try {
                                UserPlan.valueOf(resolvedPlanString)
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

    override fun fetchSubscriptionStatus(): Flow<UserSubscription> = callbackFlow {

        val uid = googleSignInClient.getUserId()

        if(uid.isBlank()) {
            close()
            awaitClose {  }
            return@callbackFlow
        }

        val ref = firebaseDatabase.getReference("subscriptions")
            .child(uid)

        val listener = object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val status = p0.getValue(UserSubscription::class.java)

                status?.let {
                    val resolvedPlan = if (it.overrideEnabled && it.forcedPlan != null) {
                        it.forcedPlan
                    } else {
                        it.plan
                    }
                    
                    val finalStatus = it.copy(plan = resolvedPlan)
                    
                    Log.i("PLAN", finalStatus.toString())
                    trySend(finalStatus)
                }
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

    override suspend fun fetchAllPackages(): Resource<List<Package>> {
        return revenueCatDataSource.fetchAllPackages()
    }
}
