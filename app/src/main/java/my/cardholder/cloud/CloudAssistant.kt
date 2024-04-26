package my.cardholder.cloud

import android.content.Intent

interface CloudAssistant {
    val isCloudAvailable: Boolean
    val signInIntent: Intent
    suspend fun downloadAppData(): Result<String>
    suspend fun uploadAppData(data: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
}
