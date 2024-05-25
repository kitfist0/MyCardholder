package my.cardholder.cloud

typealias FileNameAndContent = Pair<String, String>
fun FileNameAndContent.getName() = first
fun FileNameAndContent.getContent() = second

data class CloudFile(val fileNameAndContent: FileNameAndContent, val timestamp: Long)

interface CloudAssistant {
    suspend fun delete(fileName: String): Result<Unit>
    suspend fun download(): Result<List<CloudFile>>
    suspend fun upload(fileNameAndContent: FileNameAndContent): Result<CloudFile>
}
