package com.aarav.geowav.domain.authentication

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GoogleSignInClient @Inject constructor(val context : Context) {
    private val tag = "GoogleSignInClient"

    @Inject
    lateinit var credentialManager: CredentialManager

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    fun isLoggedIn() : Boolean{
        if(firebaseAuth.currentUser != null){
            return true
        }
        else{
            return false
        }
    }

    suspend fun signIn() : Boolean{
        try {
            val result = buildCredentialRequest()
            return handleSignIn(result)
       }
        catch (e : Exception){
            if(e is CancellationException)
                throw e
            Log.e(tag, "signIn error: ${e.message}")
            return false
        }
    }

    private suspend fun handleSignIn(result : GetCredentialResponse) : Boolean{
        val credential = result.credential

        if(credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ){
            try{
                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                val authCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken, null)

                val authResult = firebaseAuth.signInWithCredential(authCredential).await()

                return authResult.user != null
            }
            catch (e : GoogleIdTokenParsingException){
                e.printStackTrace()
                return false
            }
        }
        else{
            Log.i(tag, "handleSignIn : Invalid Credential")
            return false
        }
    }

    private suspend fun buildCredentialRequest() : GetCredentialResponse{
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId("") // Generate Client Id
                    .setAutoSelectEnabled(false)
                    .build()
            )
            .build()

        return credentialManager.getCredential(request = request, context = context)
    }

    private suspend fun signOut(){
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )

        firebaseAuth.signOut()
    }
}