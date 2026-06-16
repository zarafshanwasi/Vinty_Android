package com.idealink.vinty.utils

import android.content.Context
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.idealink.vinty.BuildConfig
import kotlinx.coroutines.launch

/**
 * Helper class to handle Google Sign In functionality.
 * Provides reusable methods for authenticating users via Google.
 */
object GoogleSignInHelper {

    /**
     * Initiates Google Sign In flow
     * 
     * @param fragment The fragment initiating the sign in
     * @param onSuccess Callback invoked with the Google ID token on successful authentication
     * @param onError Optional callback invoked with error message on failure
     */
    fun signIn(
        fragment: Fragment,
        onSuccess: (String) -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        val context = fragment.requireContext()
        val lifecycleOwner = fragment.viewLifecycleOwner
        
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleOwner.lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                val credential = result.credential

                if (
                    credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = try {
                        GoogleIdTokenCredential.createFrom(credential.data)
                    } catch (_: GoogleIdTokenParsingException) {
                        val errorMsg = "Failed to parse Google ID token"
                        handleError(context, errorMsg, onError)
                        return@launch
                    }

                    onSuccess(googleIdTokenCredential.idToken)
                } else {
                    val errorMsg = "Unexpected credential type"
                    handleError(context, errorMsg, onError)
                }
            } catch (_: NoCredentialException) {
                val errorMsg = "No Google account available"
                handleError(context, errorMsg, onError)
            } catch (e: GetCredentialException) {
                val errorMsg = "Google Sign-in failed: ${e.message}"
                handleError(context, errorMsg, onError)
            }
        }
    }

    private fun handleError(
        context: Context,
        message: String,
        onError: ((String) -> Unit)?
    ) {
        if (onError != null) {
            onError(message)
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
