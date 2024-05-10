package my.cardholder.cloud

typealias FileNameAndContent = Pair<String, String>
fun FileNameAndContent.getName() = first
fun FileNameAndContent.getContent() = second

interface CloudAssistant {
    suspend fun delete(vararg fileName: String): Result<Unit>
    suspend fun download(): Result<List<FileNameAndContent>>
    suspend fun upload(vararg fileNameAndContent: FileNameAndContent): Result<Unit>
}
