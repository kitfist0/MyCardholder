package my.cardholder.cloud

interface CloudAssistant {
    suspend fun downloadFiles(names: List<String>): Result<List<String>>
    suspend fun uploadFiles(namesAndContents: List<Pair<String, String>>): Result<Unit>
}
