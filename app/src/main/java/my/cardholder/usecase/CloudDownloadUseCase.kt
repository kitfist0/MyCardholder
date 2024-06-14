package my.cardholder.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import my.cardholder.cloud.backup.CloudBackupAssistant
import my.cardholder.data.BackupRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.BackupOperationType
import my.cardholder.data.model.BackupResult
import javax.inject.Inject

class CloudDownloadUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
    private val cloudBackupAssistant: CloudBackupAssistant,
    private val settingsRepository: SettingsRepository,
) {
    fun execute(): Flow<BackupResult> = flow {
        if (!settingsRepository.cloudSyncEnabled.first()) {
            throw Throwable("Cloud sync disabled!")
        }
        val latestSyncedVersion = settingsRepository.latestSyncedVersion.first() ?: 0
        val cloudBackupVersion = cloudBackupAssistant.getBackupVersion().getOrThrow()
        if (cloudBackupVersion > latestSyncedVersion) {
            cloudBackupAssistant.downloadBackupContent().getOrThrow()
                ?.let { emitAll(backupRepository.importFromBackupFile(it.byteInputStream())) }
                ?: emit(BackupResult.Success(BackupOperationType.IMPORT))
        } else {
            emit(BackupResult.Success(BackupOperationType.IMPORT))
        }
    }.catch {
        emit(BackupResult.Error(it.message.orEmpty()))
    }
}
