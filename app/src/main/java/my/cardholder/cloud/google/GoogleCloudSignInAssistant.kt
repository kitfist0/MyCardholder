package my.cardholder.cloud.google

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import my.cardholder.cloud.CloudSignInAssistant
import my.cardholder.util.GoogleCredentialWrapper
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GoogleCloudSignInAssistant(
    private val context: Context,
    private val googleCredentialWrapper: GoogleCredentialWrapper,
    private val googleSignInClient: GoogleSignInClient,
) : CloudSignInAssistant {

    override val alreadySignedIn: Boolean
        get() = googleCredentialWrapper.getCredential() != null

    override val signedInAccountName: String?
        get() = GoogleSignIn.getLastSignedInAccount(context)?.account?.name

    override val signInIntent: Intent
        get() = googleSignInClient.signInIntent

    override suspend fun onSignInResult(activityResult: ActivityResult): Result<Unit> {
        return if (activityResult.resultCode == -1 && alreadySignedIn) {
            Result.success(Unit)
        } else {
            Result.failure(Throwable())
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return suspendCoroutine { continuation ->
            googleSignInClient.signOut()
                .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }
    }
}
