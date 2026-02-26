package com.aarav.geowav.data.repository

import com.aarav.geowav.platform.SocialEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    val firebaseDatabase: FirebaseDatabase,
    val firebaseAuth: FirebaseAuth
) {
    private val _events = MutableSharedFlow<SocialEvent>()
    val events = _events.asSharedFlow()

    private val attachedMembers = mutableSetOf<String>()

    private var inviteRef: DatabaseReference? = null
    private var circleEventRef: DatabaseReference? = null
    private var sharingRef: DatabaseReference? = null

    private val listenerMap = mutableMapOf<String, ValueEventListener>()
    private val sharingStateCache = mutableMapOf<String, Boolean>()

    private val repositoryScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun startListening(members: List<String>) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        listenToInvites(userId)
        listenToCircle(userId)
        members.forEach { member ->
            if (!attachedMembers.contains(member)) {
                attachSharingListener(member, userId)
                attachedMembers.add(member)
            }
        }
    }

    private fun listenToInvites(userId: String) {
        inviteRef = firebaseDatabase.getReference("circle_requests").child(userId)

        inviteRef?.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {
                val status = snapshot.child("status").getValue(String::class.java)
                val sender = snapshot.child("senderProfileName").getValue(String::class.java)

                if (status == "pending" && sender != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        _events.emit(
                            SocialEvent.InviteReceived(
                                senderId = snapshot.key ?: "",
                                senderName = sender
                            )
                        )
                    }
                }
            }

            override fun onChildChanged(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun listenToCircle(userId: String) {
        circleEventRef = firebaseDatabase.getReference("circle").child(userId)

        circleEventRef?.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {

            }

            override fun onChildChanged(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {
                val userName = snapshot.child("profileName").getValue(String::class.java)
                val circleId = snapshot.key
                val status = snapshot.child("status").getValue(String::class.java)

                if (status == "accepted" && userName != null && circleId != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        _events.emit(
                            SocialEvent.InviteAccepted(
                                circleId,
                                userName
                            )
                        )
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

//    private fun listenToSharing(userId: String) {
//        sharingRef = firebaseDatabase.getReference("live_location")
//
//        var ownerId: String = ""
//
//        sharingRef?.addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(
//                snapshot: DataSnapshot,
//                previousChildName: String?
//            ) {
//                ownerId = snapshot.key ?: return
//                val isSharing = snapshot.child("active").getValue(Boolean::class.java)
//
//                if (userId == ownerId) return
//
//                val sharedWith = snapshot.child("sharedWith").getValue(List::class.java) ?: return
//
//                if (sharedWith.contains(userId)) {
//                    fetchUserName(ownerId) { username ->
//                        CoroutineScope(Dispatchers.IO).launch {
//                            if (isSharing == true) {
//                                _events.emit(
//                                    SocialEvent.SharingStarted(
//                                        ownerId,
//                                        username
//                                    )
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
//            override fun onChildChanged(
//                snapshot: DataSnapshot,
//                previousChildName: String?
//            ) {
//
//            }
//
//            override fun onChildRemoved(snapshot: DataSnapshot) {
//                fetchUserName(ownerId) { username ->
//                    if (ownerId == snapshot.key) {
//                        CoroutineScope(Dispatchers.IO).launch {
//                            _events.emit(
//                                SocialEvent.SharingStopped(
//                                    ownerId,
//                                    username
//                                )
//                            )
//                        }
//                    }
//                }
//            }
//
//            override fun onChildMoved(
//                snapshot: DataSnapshot,
//                previousChildName: String?
//            ) {
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//
//        })
//    }

    private fun fetchUserName(userId: String, onResult: (String) -> Unit) {

        firebaseDatabase.getReference("users")
            .child(userId)
            .child("username")
            .get()
            .addOnSuccessListener { snapshot ->
                val name = snapshot.getValue(String::class.java) ?: "User"
                onResult(name)
            }
    }

    private fun attachSharingListener(memberId: String, myUserId: String) {


        // Prevent duplicate listeners
        if (listenerMap.containsKey(memberId)) return

        val ref = firebaseDatabase.getReference("live_location")
            .child(memberId)

        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {


                val isSharingWithMe =
                    snapshot.exists() &&
                            snapshot.child("sharedWith")
                                .children
                                .mapNotNull { it.getValue(String::class.java) }
                                .contains(myUserId)


                val previous = sharingStateCache[memberId]

                if (previous == null) {
                    sharingStateCache[memberId] = isSharingWithMe
                    return
                }

                if (previous != isSharingWithMe) {

                    sharingStateCache[memberId] = isSharingWithMe

                    fetchUserName(memberId) { username ->

                        repositoryScope.launch {

                            if (isSharingWithMe) {
                                _events.emit(
                                    SocialEvent.SharingStarted(
                                        memberId,
                                        username
                                    )
                                )
                            } else {
                                _events.emit(
                                    SocialEvent.SharingStopped(
                                        memberId,
                                        username
                                    )
                                )
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun stopListening() {

        listenerMap.forEach { (memberId, listener) ->
            firebaseDatabase.getReference("live_location")
                .child(memberId)
                .removeEventListener(listener)
        }

        listenerMap.clear()
        sharingStateCache.clear()
        repositoryScope.cancel()
    }
}