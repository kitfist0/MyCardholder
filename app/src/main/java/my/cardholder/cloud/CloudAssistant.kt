package my.cardholder.cloud

interface CloudAssistant {
    fun downloadAppData(): Result<String>
    fun uploadAppData(data: String): Result<Unit>
}
