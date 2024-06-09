package my.cardholder.cloud

typealias FileVersion = Long

interface CloudAssistant {
    suspend fun getFileVersion(): Result<FileVersion>
    suspend fun deleteFile(): Result<Unit>
    suspend fun downloadFileContent(): Result<String?>
    suspend fun uploadFileContent(content: String): Result<FileVersion>
}
