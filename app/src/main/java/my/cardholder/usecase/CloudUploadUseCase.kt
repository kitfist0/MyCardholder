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
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class CloudUploadUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
    private val cloudBackupAssistant: CloudBackupAssistant,
) {

    fun execute(checksum: BackupChecksum): Flow<Result<BackupChecksum>> = uploadBackup(checksum)

    private fun uploadBackup(
        checksum: BackupChecksum,
        outputStream: ByteArrayOutputStream = ByteArrayOutputStream(),
    ): Flow<Result<BackupChecksum>> = flow {
        emit(Result.Loading("Getting the backup checksum"))
        if (checksum == cloudBackupAssistant.getBackupChecksum().getOrNull()) {
            emit(Result.Success(checksum))
        } else {
            val resultFlow = backupRepository.exportToBackupFile(outputStream)
                .map { backupResult ->
                    when (backupResult) {
                        is BackupResult.Error -> throw Throwable(backupResult.message)
                        is BackupResult.Progress -> Result.Loading("Uploading backup data")
                        is BackupResult.Success -> {
                            cloudBackupAssistant.uploadBackup(String(outputStream.toByteArray()), checksum)
                                .onFailure { throwable -> throw throwable }
                            Result.Success(checksum)
                        }
                    }
                }
            emitAll(resultFlow)
        }
    }.catch {
        emit(Result.Error(it))
    }
}
