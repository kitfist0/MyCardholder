package my.cardholder.cloud

interface CloudAssistant {
    val isCloudAvailable: Boolean
    suspend fun downloadAppData(): Result<String>
    suspend fun uploadAppData(data: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
}
