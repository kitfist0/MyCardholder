package my.cardholder.cloud.backup

typealias BackupVersion = Long

interface CloudBackupAssistant {
    suspend fun getBackupVersion(): Result<BackupVersion>
    suspend fun deleteBackup(): Result<Unit>
    suspend fun downloadBackupContent(): Result<String?>
    suspend fun uploadBackupContent(content: String): Result<BackupVersion>
}
