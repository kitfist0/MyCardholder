package my.cardholder.cloud

interface CloudAssistant {
    suspend fun downloadFiles(): Result<List<Pair<String, String>>>
    suspend fun uploadFiles(namesAndContents: List<Pair<String, String>>): Result<Unit>
}
