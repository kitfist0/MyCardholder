package my.cardholder.cloud.signin

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import my.cardholder.util.GoogleCredentialWrapper
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GoogleCloudSignInAssistant(
    private val googleCredentialWrapper: GoogleCredentialWrapper,
    private val googleSignInClient: GoogleSignInClient,
) : CloudSignInAssistant {

    override val alreadySignedIn: Boolean
        get() = googleCredentialWrapper.getCredential() != null

    override val signInIntent: Intent
        get() = googleSignInClient.signInIntent

    override suspend fun signOut(): Result<Unit> {
        return suspendCoroutine { continuation ->
            googleSignInClient.signOut()
                .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }
    }
}
