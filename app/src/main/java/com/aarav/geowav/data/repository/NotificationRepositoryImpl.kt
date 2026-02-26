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

    // Shared flow in order to notify user based on specific event
    private val _events = MutableSharedFlow<SocialEvent>()
    // Service uses this to observe events
    val events = _events.asSharedFlow()

    // Track attached members
    private val attachedMembers = mutableSetOf<String>()

    // Access circle_requests
    private var inviteRef: DatabaseReference? = null
    // Access circle
    private var circleEventRef: DatabaseReference? = null

    // Store user and listener references preventing duplicate listener to avoid memory leaks
    private val listenerMap = mutableMapOf<String, ValueEventListener>()

    // Store sharing state to avoid duplicate notifications
    private val sharingStateCache = mutableMapOf<String, Boolean>()

    // Using coroutine scope to avoid leaks
    private val repositoryScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Listen to all events
    fun startListening(members: List<String>) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        listenToInvites(userId)
        listenToCircle(userId)
        members.forEach { member ->
            // Attach listener to each member only if not already attached
            if (!attachedMembers.contains(member)) {
                attachSharingListener(member, userId)
                // Add member to attached members
                attachedMembers.add(member)
            }
        }
    }

    // Listen to new invite events and notify user
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

    // Listen for circle updates(specifically status change to accepted) and notify user
    private fun listenToCircle(userId: String) {
        circleEventRef = firebaseDatabase.getReference("circle").child(userId)

        circleEventRef?.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {

            }

            // When status changes to accepted, notify user
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

    // Fetch username from database
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

    // Attach listener to each member
    private fun attachSharingListener(memberId: String, myUserId: String) {


        //  Prevent attaching multiple listeners for same member
        if (listenerMap.containsKey(memberId)) return

        // Reference this member's live location node
        val ref = firebaseDatabase.getReference("live_location")
            .child(memberId)

        // Attach real-time listener
        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {


                // Determine if this member is currently sharing
                // location with me
                val isSharingWithMe =
                    snapshot.exists() &&
                            snapshot.child("sharedWith")
                                .children
                                .mapNotNull { it.getValue(String::class.java) }
                                .contains(myUserId)


                // sharingStateCache stores last known state for each member
                val previous = sharingStateCache[memberId]

                // Ignore the first Firebase snapshot (it represents current state, not a change)
                // Cache it only to prevent fake notifications on service restart or app launch
                if (previous == null) {
                    sharingStateCache[memberId] = isSharingWithMe
                    return
                }

                // Only react if state actually changed, if different emit event
                if (previous != isSharingWithMe) {

                    // Update cache to new state
                    sharingStateCache[memberId] = isSharingWithMe

                    fetchUserName(memberId) { username ->

                        repositoryScope.launch {

                            // Member started sharing
                            if (isSharingWithMe) {
                                _events.emit(
                                    SocialEvent.SharingStarted(
                                        memberId,
                                        username
                                    )
                                )
                            } else {
                                // Member stopped sharing
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