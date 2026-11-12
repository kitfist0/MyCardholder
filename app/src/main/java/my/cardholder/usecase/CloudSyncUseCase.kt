package my.cardholder.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import my.cardholder.cloud.BackupChecksum
import my.cardholder.cloud.CloudBackupAssistant
import my.cardholder.data.BackupRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.data.SyncRepository
import my.cardholder.data.model.CloudProvider
import my.cardholder.di.Google
import my.cardholder.di.Yandex
import my.cardholder.util.Result
import javax.inject.Inject

/**
 * Two-way sync: merges cloud backups into the local database, then uploads the merged state.
 *
 * Backup file names are checksums of the synced state, so equal names mean equal content.
 * A new backup is uploaded before the old ones are deleted, so the cloud is never left empty.
 */
class CloudSyncUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
    private val settingsRepository: SettingsRepository,
    private val syncRepository: SyncRepository,
    @Google private val googleCloudBackupAssistant: CloudBackupAssistant,
    @Yandex private val yandexCloudBackupAssistant: CloudBackupAssistant,
) {

    /** Runs off the main thread: merging writes barcode images, parsing and hashing are heavy too */
    fun execute(cloudProvider: CloudProvider): Flow<Result<BackupChecksum>> = flow {
        emit(Result.Loading("Checking the cloud backup"))
        val cloudBackupAssistant = getCloudBackupAssistant(cloudProvider)
        val cloudChecksums = cloudBackupAssistant.getBackupChecksums().getOrThrow()
        val latestSyncedChecksum = settingsRepository.latestSyncedBackupChecksum.first()
        val localChecksum = syncRepository.getLocalChecksum()

        // The latest synced backup is already a part of the local state
        cloudChecksums
            .filter { checksum -> checksum != localChecksum && checksum != latestSyncedChecksum }
            .forEach { checksum ->
                emit(Result.Loading("Downloading backup data"))
                // Null if another device has just replaced the backup, its new one will be merged next time
                val content = cloudBackupAssistant.getBackupContent(checksum).getOrThrow()
                    ?: return@forEach
                emit(Result.Loading("Merging backup data"))
                syncRepository.mergeSnapshot(backupRepository.decodeSyncSnapshot(content))
            }

        val snapshot = syncRepository.getLocalSnapshot()
        val syncedChecksum = syncRepository.calculateChecksum(snapshot)
        if (cloudChecksums != listOf(syncedChecksum)) {
            if (syncedChecksum !in cloudChecksums) {
                emit(Result.Loading("Uploading backup data"))
                val content = backupRepository.encodeSyncSnapshot(snapshot)
                cloudBackupAssistant.uploadBackup(content, syncedChecksum).getOrThrow()
            }
            // Only the backups that were merged are deleted, a failure leaves them for the next sync
            cloudBackupAssistant.deleteBackups(cloudChecksums - syncedChecksum)
        }
        settingsRepository.setLatestSyncedBackupChecksum(syncedChecksum)
        emit(Result.Success(syncedChecksum))
    }.catch {
        emit(Result.Error(it))
    }.flowOn(Dispatchers.IO)

    private fun getCloudBackupAssistant(cloudProvider: CloudProvider): CloudBackupAssistant {
        return when (cloudProvider) {
            CloudProvider.GOOGLE -> googleCloudBackupAssistant
            CloudProvider.YANDEX -> yandexCloudBackupAssistant
        }
    }
}
