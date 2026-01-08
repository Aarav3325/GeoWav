package com.aarav.geowav.data.authentication

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.aarav.geowav.R
import com.aarav.geowav.data.model.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GoogleSignInClient @Inject constructor(
    @ApplicationContext val context: Context, val firebaseAuth: FirebaseAuth,
    val firebaseDatabase: FirebaseDatabase
) {
    private val tag = "GoogleSignInClient"

    private val userReference = firebaseDatabase.getReference("users")

    @Inject
    lateinit var credentialManager: CredentialManager


    fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    suspend fun signIn(): Boolean {
        try {
            val result = buildCredentialRequest()
            return handleSignIn(result)
        } catch (e: Exception) {
            if (e is CancellationException)
                throw e
            Log.e(tag, "signIn error: ${e.message}")
            return false
        }
    }

    private suspend fun handleSignIn(result: GetCredentialResponse): Boolean {
        val credential = result.credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val tokenCredential = GoogleIdTokenCredential.Companion.createFrom(credential.data)

                val authCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken, null)

                val authResult = firebaseAuth.signInWithCredential(authCredential).await()

                if (authResult.user != null) {
                    storeUserData(authResult.user?.email ?: "", authResult.user?.displayName ?: "")
                }

                return authResult.user != null
            } catch (e: GoogleIdTokenParsingException) {
                e.printStackTrace()
                return false
            }
        } else {
            Log.i(tag, "handleSignIn : Invalid Credential")
            return false
        }
    }

    private suspend fun buildCredentialRequest(): GetCredentialResponse {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.google_client_id)) // Generate Client Id
                    .setAutoSelectEnabled(false)
                    .build()
            )
            .build()

        return credentialManager.getCredential(request = request, context = context)
    }

    suspend fun signOut() {
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )

        firebaseAuth.signOut()
    }

    suspend fun signUpUsingEmailAndPassword(
        username: String,
        email: String,
        password: String
    ): Boolean {
        return try {

            val finalEmail = email.trim()
            val finalPass = password.trim()

            if (finalEmail.isBlank() || finalPass.isBlank()) return false

            firebaseAuth
                .createUserWithEmailAndPassword(finalEmail, finalPass)
                .await()

            val user = firebaseAuth.currentUser ?: return false

            storeUserData(finalEmail, username)

            true
        } catch (e: Exception) {
            Log.e(tag, "signUp failed", e)
            false
        }
    }


    suspend fun signInWithEmailAndPassword(
        email: String, password: String
    ): Boolean {
        try {
            val finalEmail = email.trim()
            val finalPass = password.trim()

            if (finalEmail.isBlank() || finalPass.isBlank()) return false


            firebaseAuth.signInWithEmailAndPassword(finalEmail, finalPass).await()
            return true
        } catch (e: Exception) {
            Log.i(tag, e.message.toString())
            return false
        }
    }


    fun storeUserData(email: String, username: String) {
        val userId = getUserId()

        userId.let {
            val user = User(
                userId,
                username.trim(),
                email
            )

            userReference.child(userId).setValue(user)
                .addOnSuccessListener {
                    Log.d(tag, "userReference: Success")
                }
                .addOnFailureListener {
                    Log.e(tag, "userReference: Success")
                }
        }
    }

    fun getUserId(): String {
        return firebaseAuth.currentUser?.uid ?: ""
    }

    fun getUserName(): String {
        return firebaseAuth.currentUser?.displayName ?: ""
    }

    fun getUserProfile(): Uri {
        return firebaseAuth.currentUser?.photoUrl ?: Uri.EMPTY
    }
}