package my.cardholder.cloud.signin

import android.content.Intent

interface CloudSignInAssistant {
    val alreadySignedIn: Boolean
    val signInIntent: Intent
    suspend fun signOut(): Result<Unit>
}
