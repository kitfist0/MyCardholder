package my.cardholder.cloud.signin

import android.content.Intent
import androidx.activity.result.ActivityResult

interface CloudSignInAssistant {
    val alreadySignedIn: Boolean
    val signInIntent: Intent
    suspend fun onSignInResult(activityResult: ActivityResult): Result<Unit>
    suspend fun signOut(): Result<Unit>
}
