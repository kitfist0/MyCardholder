// package my.cardholder.cloud

// typealias BackupChecksum = Long

// interface CloudBackupAssistant {
//     suspend fun getBackupChecksum(): Result<BackupChecksum>
//     suspend fun getBackupContent(checksum: BackupChecksum): Result<String?>
//     suspend fun deleteBackup(): Result<Unit>
//     suspend fun uploadBackup(content: String, checksum: BackupChecksum): Result<Unit>

//     companion object {
//         fun String.fileNameToChecksum(): BackupChecksum {
//             return toLong()
//         }
//     }
// }
