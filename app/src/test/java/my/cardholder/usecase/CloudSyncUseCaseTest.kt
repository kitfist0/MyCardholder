package my.cardholder.usecase

import app.cash.turbine.Event
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import my.cardholder.cloud.CloudBackupAssistant
import my.cardholder.data.BackupRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.data.SyncRepository
import my.cardholder.data.model.CloudProvider
import my.cardholder.data.model.SyncSnapshot
import my.cardholder.util.Result
import kotlin.Result as KotlinResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CloudSyncUseCaseTest {

    private val backupRepository = mockk<BackupRepository>()
    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)
    private val syncRepository = mockk<SyncRepository>(relaxed = true)
    private val googleAssistant = mockk<CloudBackupAssistant>()
    private val yandexAssistant = mockk<CloudBackupAssistant>()
    private val useCase = CloudSyncUseCase(
        backupRepository,
        settingsRepository,
        syncRepository,
        googleAssistant,
        yandexAssistant,
    )

    private val localSnapshot = SyncSnapshot(emptyList(), emptyList())
    private val remoteSnapshot = SyncSnapshot(emptyList(), emptyList())

    @Before
    fun setUp() {
        every { settingsRepository.latestSyncedBackupChecksum } returns MutableStateFlow(LATEST_SYNCED)
        coEvery { syncRepository.getLocalChecksum() } returns LOCAL
        coEvery { syncRepository.getLocalSnapshot() } returns localSnapshot
        every { syncRepository.calculateChecksum(localSnapshot) } returns MERGED
        coEvery { backupRepository.encodeSyncSnapshot(localSnapshot) } returns "local content"
        coEvery { backupRepository.decodeSyncSnapshot("remote content") } returns remoteSnapshot
        coEvery { googleAssistant.getBackupContent(any()) } returns KotlinResult.success("remote content")
        coEvery { googleAssistant.uploadBackup(any(), any()) } returns KotlinResult.success(Unit)
        coEvery { googleAssistant.deleteBackups(any()) } returns KotlinResult.success(Unit)
    }

    @Test
    fun `new cloud backup is merged, merged state is uploaded and old backup is deleted`() = runTest {
        coEvery { googleAssistant.getBackupChecksums() } returns KotlinResult.success(listOf(REMOTE))

        useCase.execute(CloudProvider.GOOGLE).test {
            assertEquals(Result.Success(MERGED), expectMostRecentItemAfterCompletion())
        }

        coVerify { syncRepository.mergeSnapshot(remoteSnapshot) }
        coVerify { googleAssistant.uploadBackup("local content", MERGED) }
        coVerify { googleAssistant.deleteBackups(listOf(REMOTE)) }
        coVerify { settingsRepository.setLatestSyncedBackupChecksum(MERGED) }
    }

    @Test
    fun `already synced cloud backup is not downloaded`() = runTest {
        coEvery { googleAssistant.getBackupChecksums() } returns KotlinResult.success(listOf(LATEST_SYNCED, LOCAL))

        useCase.execute(CloudProvider.GOOGLE).test {
            assertEquals(Result.Success(MERGED), expectMostRecentItemAfterCompletion())
        }

        coVerify(exactly = 0) { googleAssistant.getBackupContent(any()) }
        coVerify(exactly = 0) { syncRepository.mergeSnapshot(any()) }
        coVerify { googleAssistant.uploadBackup("local content", MERGED) }
        coVerify { googleAssistant.deleteBackups(listOf(LATEST_SYNCED, LOCAL)) }
    }

    @Test
    fun `nothing is uploaded when cloud already has the synced state`() = runTest {
        coEvery { googleAssistant.getBackupChecksums() } returns KotlinResult.success(listOf(MERGED))

        useCase.execute(CloudProvider.GOOGLE).test {
            assertEquals(Result.Success(MERGED), expectMostRecentItemAfterCompletion())
        }

        coVerify(exactly = 0) { googleAssistant.uploadBackup(any(), any()) }
        coVerify(exactly = 0) { googleAssistant.deleteBackups(any()) }
        coVerify { settingsRepository.setLatestSyncedBackupChecksum(MERGED) }
    }

    @Test
    fun `extra backups are deleted without uploading when cloud has the synced state`() = runTest {
        coEvery { googleAssistant.getBackupChecksums() } returns KotlinResult.success(listOf(MERGED, REMOTE))

        useCase.execute(CloudProvider.GOOGLE).test {
            assertEquals(Result.Success(MERGED), expectMostRecentItemAfterCompletion())
        }

        coVerify(exactly = 0) { googleAssistant.uploadBackup(any(), any()) }
        coVerify { googleAssistant.deleteBackups(listOf(REMOTE)) }
    }

    @Test
    fun `empty cloud gets the local state`() = runTest {
        coEvery { googleAssistant.getBackupChecksums() } returns KotlinResult.success(emptyList())

        useCase.execute(CloudProvider.GOOGLE).test {
            assertEquals(Result.Success(MERGED), expectMostRecentItemAfterCompletion())
        }

        coVerify { googleAssistant.uploadBackup("local content", MERGED) }
    }

    @Test
    fun `backup that disappeared during sync is skipped`() = runTest {
        coEvery { googleAssistant.getBackupChecksums() } returns KotlinResult.success(listOf(REMOTE))
        coEvery { googleAssistant.getBackupContent(REMOTE) } returns KotlinResult.success(null)

        useCase.execute(CloudProvider.GOOGLE).test {
            assertEquals(Result.Success(MERGED), expectMostRecentItemAfterCompletion())
        }

        coVerify(exactly = 0) { syncRepository.mergeSnapshot(any()) }
    }

    @Test
    fun `failed upload keeps old backups and does not mark state as synced`() = runTest {
        coEvery { googleAssistant.getBackupChecksums() } returns KotlinResult.success(listOf(REMOTE))
        coEvery { googleAssistant.uploadBackup(any(), any()) } returns KotlinResult.failure(Exception("No network"))

        useCase.execute(CloudProvider.GOOGLE).test {
            val result = expectMostRecentItemAfterCompletion()
            assertTrue(result is Result.Error)
            assertEquals("No network", (result as Result.Error).throwable.message)
        }

        coVerify(exactly = 0) { googleAssistant.deleteBackups(any()) }
        coVerify(exactly = 0) { settingsRepository.setLatestSyncedBackupChecksum(any()) }
    }

    @Test
    fun `failed listing is reported as error`() = runTest {
        coEvery { yandexAssistant.getBackupChecksums() } returns KotlinResult.failure(Exception("Empty token"))

        useCase.execute(CloudProvider.YANDEX).test {
            assertEquals(Result.Loading("Checking the cloud backup"), awaitItem())
            val result = awaitItem()
            assertTrue(result is Result.Error)
            awaitComplete()
        }
    }

    private suspend fun ReceiveTurbine<Result<Long>>.expectMostRecentItemAfterCompletion(): Result<Long> {
        val items = cancelAndConsumeRemainingEvents().filterIsInstance<Event.Item<Result<Long>>>()
        return items.last().value
    }

    private companion object {
        const val LATEST_SYNCED = 1L
        const val LOCAL = 2L
        const val REMOTE = 3L
        const val MERGED = 4L
    }
}
