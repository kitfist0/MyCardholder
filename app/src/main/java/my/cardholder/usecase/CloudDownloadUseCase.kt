package my.cardholder.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import my.cardholder.cloud.BackupChecksum
import my.cardholder.cloud.CloudBackupAssistant
import my.cardholder.data.BackupRepository
import my.cardholder.data.model.BackupResult
import my.cardholder.util.Result
import javax.inject.Inject

class CloudDownloadUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
    private val cloudBackupAssistant: CloudBackupAssistant,
) {
    fun execute(checksum: BackupChecksum): Flow<Result<BackupChecksum>> = flow {
        emit(Result.Loading("Getting the backup checksum"))
        val cloudBackupChecksum = cloudBackupAssistant.getBackupChecksum().getOrThrow()
        if (cloudBackupChecksum > checksum) {
            emit(Result.Loading("Downloading backup data"))
            val content = cloudBackupAssistant.getBackupContent(cloudBackupChecksum).getOrThrow()
            content ?: throw Throwable("No cloud backup data!")
            val resultFlow = backupRepository.importFromBackupFile(content.byteInputStream())
                .map { backupResult ->
                    when (backupResult) {
                        is BackupResult.Error -> throw Throwable(backupResult.message)
                        is BackupResult.Progress -> Result.Loading("Importing backup data")
                        is BackupResult.Success -> Result.Success(cloudBackupChecksum)
                    }
                }
            emitAll(resultFlow)
        } else {
            emit(Result.Success(checksum))
        }
    }.catch {
        emit(Result.Error(it))
    }
}
