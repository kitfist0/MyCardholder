package my.cardholder.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import my.cardholder.cloud.backup.BackupChecksum
import my.cardholder.cloud.backup.CloudBackupAssistant
import my.cardholder.data.BackupRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.BackupResult
import my.cardholder.util.Result
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class CloudUploadUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
    private val cloudBackupAssistant: CloudBackupAssistant,
    private val settingsRepository: SettingsRepository,
) {

    private companion object {
        const val SUCCESS_MESSAGE = "Upload completed!"
    }

    fun execute(checksum: BackupChecksum): Flow<Result<String>> = uploadBackup(checksum)

    private fun uploadBackup(
        checksum: BackupChecksum,
        outputStream: ByteArrayOutputStream = ByteArrayOutputStream(),
    ): Flow<Result<String>> = flow {
        if (!settingsRepository.cloudSyncEnabled.first()) {
            throw Throwable("Cloud sync disabled!")
        }
        emit(Result.Loading("Getting the backup checksum"))
        if (checksum == cloudBackupAssistant.getBackupChecksum().getOrNull()) {
            emit(Result.Success(SUCCESS_MESSAGE))
        } else {
            val resultFlow = backupRepository.exportToBackupFile(outputStream)
                .map { backupResult ->
                    when (backupResult) {
                        is BackupResult.Error -> throw Throwable(backupResult.message)
                        is BackupResult.Progress -> Result.Loading("Uploading backup data")
                        is BackupResult.Success -> {
                            cloudBackupAssistant.uploadBackup(String(outputStream.toByteArray()), checksum)
                                .onFailure { throwable -> throw throwable }
                            settingsRepository.setLatestSyncedBackupChecksum(checksum)
                            Result.Success(SUCCESS_MESSAGE)
                        }
                    }
                }
            emitAll(resultFlow)
        }
    }.catch {
        emit(Result.Error(it))
    }
}
