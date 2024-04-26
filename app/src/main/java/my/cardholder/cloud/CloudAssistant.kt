package my.cardholder.cloud

interface CloudAssistant {
    suspend fun downloadAppData(): Result<String>
    suspend fun uploadAppData(data: String): Result<Unit>
}
