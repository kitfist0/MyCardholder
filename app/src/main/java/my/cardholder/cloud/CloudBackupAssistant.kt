package my.cardholder.cloud

typealias BackupChecksum = Long

interface CloudBackupAssistant {
    /** Checksums of all backups in the cloud. Normally there is one, but concurrent syncs can leave more */
    suspend fun getBackupChecksums(): Result<List<BackupChecksum>>
    suspend fun getBackupContent(checksum: BackupChecksum): Result<String?>
    suspend fun deleteBackups(checksums: Collection<BackupChecksum>): Result<Unit>
    /** Adds a new backup, existing backups are kept */
    suspend fun uploadBackup(content: String, checksum: BackupChecksum): Result<Unit>

    companion object {
        fun String.fileNameToChecksum(): BackupChecksum? {
            return toLongOrNull()
        }
    }
}
