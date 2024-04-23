package my.cardholder.cloud

interface CloudAssistant {
    val isCloudAvailable: Boolean
    fun downloadAppData(): Result<String>
    fun uploadAppData(data: String): Result<Unit>
    fun signOut()
}
