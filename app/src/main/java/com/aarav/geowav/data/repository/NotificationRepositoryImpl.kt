package com.aarav.geowav.data.repository

import android.util.Log
import com.aarav.geowav.data.mapper.FirebaseActivity
import com.aarav.geowav.data.mapper.toGeoAlert
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
    private val geofenceListenerMap = mutableMapOf<String, ChildEventListener>()

    private val emergencyListenerMap = mutableMapOf<String, ValueEventListener>()

    // Store sharing state to avoid duplicate notifications
    private val sharingStateCache = mutableMapOf<String, Boolean>()

    private val emergencyStateCache = mutableMapOf<String, Boolean>()
    private val geofenceCache = mutableMapOf<String, Boolean>()

    // Using coroutine scope to avoid leaks
    private val repositoryScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Listen to all events
    fun startListening(members: List<String>) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        listenToInvites(userId)
        listenToCircle(userId)
        members.forEach { memberId ->

            // Attach sharing listener if not already attached
            if (!listenerMap.containsKey(memberId)) {
                attachSharingListener(memberId, userId)
            }

            if (!geofenceListenerMap.containsKey(memberId)) {
                listenToGeofence(memberId)
            }

            // Attach emergency listener if not already attached
            if (!emergencyListenerMap.containsKey(memberId)) {
                attachEmergencySharingListener(memberId, userId)
            }
        }
    }

    private fun listenToGeofence(memberId: String) {

        if (geofenceListenerMap.contains(memberId)) return


        Log.i("NOTI", "listenToGeofenceCalled")

        val geofenceRef = firebaseDatabase.getReference("geofence_activity")
            .child(memberId)

        // Record when this listener starts so we can skip old events
        // onChildAdded fires for ALL existing children on first attach
        val listenerStartTime = System.currentTimeMillis()

        val listener = object : ChildEventListener {
            override fun onChildAdded(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {
                val geofence = snapshot.getValue(FirebaseActivity::class.java)
                val geoAlert = geofence?.toGeoAlert(id = snapshot.key ?: "")

                // Skip events that existed before this listener started
                val eventTime = geofence?.timestamp ?: 0L
                if (eventTime < listenerStartTime) return

                Log.i("NOTI", "onChildAdded: $geoAlert")
                if (geoAlert != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        _events.emit(SocialEvent.Geofence(geoAlert))
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

        }

        geofenceRef.addChildEventListener(listener)
        geofenceListenerMap[memberId] = listener
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
        val listener = object : ValueEventListener {

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
        }

        // Attach listener
        ref.addValueEventListener(listener)

        // Store it in map
        listenerMap[memberId] = listener
    }

    private fun attachEmergencySharingListener(memberId: String, userId: String) {
        if (emergencyListenerMap.containsKey(memberId)) return

        val ref = firebaseDatabase.getReference("emergency_sharing")
            .child(memberId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val isEmergencySharing = snapshot.exists()
                        && snapshot.child("viewers").children
                    .mapNotNull {
                        it.key
                    }.contains(userId)

                val previous = emergencyStateCache[memberId]

                if (previous == null) {
                    emergencyStateCache[memberId] = isEmergencySharing
                    return
                }

                if (previous != isEmergencySharing) {
                    emergencyStateCache[memberId] = isEmergencySharing

                    if (isEmergencySharing) {
                        fetchUserName(memberId) { username ->
                            repositoryScope.launch {
                                _events.emit(
                                    SocialEvent.EmergencyStarted(
                                        memberId,
                                        username
                                    )
                                )
                            }
                        }
                    } else {
                        fetchUserName(memberId) { username ->
                            repositoryScope.launch {
                                _events.emit(
                                    SocialEvent.EmergencyStopped(
                                        memberId,
                                        username
                                    )
                                )
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        ref.addValueEventListener(listener)
        emergencyListenerMap[memberId] = listener
    }

    fun stopListening() {

        listenerMap.forEach { (memberId, listener) ->
            firebaseDatabase.getReference("live_location")
                .child(memberId)
                .removeEventListener(listener)
        }

        geofenceListenerMap.clear()
        listenerMap.clear()
        emergencyListenerMap.clear()
        sharingStateCache.clear()
        repositoryScope.cancel()
    }
}