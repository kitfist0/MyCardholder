package my.cardholder.cloud

interface CloudAssistant {
    suspend fun downloadCsvFile(name: String): Result<String>
    suspend fun uploadCsvFile(name: String, content: String): Result<Unit>
}
