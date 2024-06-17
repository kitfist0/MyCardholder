package my.cardholder.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import my.cardholder.cloud.backup.BackupChecksum
import my.cardholder.cloud.backup.CloudBackupAssistant
import my.cardholder.data.BackupRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.BackupResult
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class CloudUploadUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
    private val cloudBackupAssistant: CloudBackupAssistant,
    private val settingsRepository: SettingsRepository,
) {

    fun execute(checksum: BackupChecksum): Flow<BackupResult> = uploadBackup(checksum)

    private fun uploadBackup(
        checksum: BackupChecksum,
        outputStream: ByteArrayOutputStream = ByteArrayOutputStream(),
    ): Flow<BackupResult> = flow {
        if (!settingsRepository.cloudSyncEnabled.first()) {
            throw Throwable("Cloud sync disabled!")
        }
        emitAll(backupRepository.exportToBackupFile(outputStream))
    }.onEach { backupResult ->
        if (backupResult is BackupResult.Success) {
            val content = String(outputStream.toByteArray())
            cloudBackupAssistant.uploadBackup(content, checksum)
                .onSuccess { settingsRepository.setLatestSyncedBackupChecksum(checksum) }
        }
    }.catch {
        emit(BackupResult.Error(it.message.orEmpty()))
    }
}
