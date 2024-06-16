package my.cardholder.cloud.backup

typealias BackupChecksum = Long

interface CloudBackupAssistant {
    suspend fun getBackupChecksum(): Result<BackupChecksum>
    suspend fun getBackupContent(checksum: BackupChecksum): Result<String?>
    suspend fun deleteBackup(): Result<Unit>
    suspend fun uploadBackup(content: String, checksum: BackupChecksum): Result<Unit>
}
