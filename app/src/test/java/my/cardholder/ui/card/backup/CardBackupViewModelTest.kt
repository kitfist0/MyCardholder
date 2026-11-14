package my.cardholder.ui.card.backup

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import my.cardholder.R
import my.cardholder.data.BackupRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.BackupError
import my.cardholder.data.model.BackupOperationType
import my.cardholder.data.model.BackupResult
import my.cardholder.ui.base.BaseEvent
import my.cardholder.util.Text
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

@OptIn(ExperimentalCoroutinesApi::class)
class CardBackupViewModelTest {

    private val backupRepository = mockk<BackupRepository>()
    private val settingsRepository = mockk<SettingsRepository>()
    private val inputStream = ByteArrayInputStream(ByteArray(0))

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { settingsRepository.cloudSyncEnabled } returns flowOf(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `import errors are shown as localized messages`() = runTest {
        val expectedMessages = mapOf(
            BackupError.NOT_BACKUP_FILE to R.string.card_backup_dialog_not_backup_file_error_message,
            BackupError.NEWER_BACKUP_VERSION to R.string.card_backup_dialog_newer_version_error_message,
            BackupError.NO_CARDS to R.string.card_backup_dialog_no_cards_error_message,
            BackupError.DAMAGED_BACKUP_FILE to R.string.card_backup_dialog_damaged_file_error_message,
            BackupError.OPERATION_FAILED to R.string.card_backup_dialog_operation_failed_error_message,
        )
        expectedMessages.forEach { (error, messageRes) ->
            assertEquals(
                listOf(BaseEvent.ToastMessage(Text.Resource(messageRes))),
                importEvents(BackupResult.Error(error)),
            )
        }
    }

    @Test
    fun `failed operation shows its details`() = runTest {
        assertEquals(
            listOf(
                BaseEvent.ToastMessage(
                    Text.ResourceAndParams(
                        R.string.card_backup_dialog_operation_failed_with_details_error_message,
                        listOf("Disk is full"),
                    )
                )
            ),
            importEvents(BackupResult.Error(BackupError.OPERATION_FAILED, "Disk is full")),
        )
    }

    @Test
    fun `import with skipped cards shows their number and closes dialog`() = runTest {
        assertEquals(
            listOf(
                BaseEvent.ToastMessage(
                    Text.ResourceAndParams(
                        R.string.card_backup_dialog_import_completed_with_skipped_cards_message,
                        listOf(2),
                    )
                ),
                BaseEvent.NavigateUp,
            ),
            importEvents(BackupResult.Success(BackupOperationType.IMPORT, numOfSkippedCards = 2)),
        )
    }

    @Test
    fun `import without skipped cards shows completed message and closes dialog`() = runTest {
        assertEquals(
            listOf(
                BaseEvent.ToastMessage(Text.Resource(R.string.card_backup_dialog_import_completed_message)),
                BaseEvent.NavigateUp,
            ),
            importEvents(BackupResult.Success(BackupOperationType.IMPORT)),
        )
    }

    private suspend fun importEvents(result: BackupResult): List<BaseEvent> {
        every { backupRepository.importFromBackupFile(inputStream) } returns flowOf(result)
        val viewModel = CardBackupViewModel(
            backupRepository,
            SavedStateHandle(mapOf("show_only_import_buttons" to true)),
            settingsRepository,
        )
        val events = mutableListOf<BaseEvent>()
        viewModel.baseEvents.test {
            viewModel.onBackupFileImportResult(inputStream)
            events.add(awaitItem())
            if (result is BackupResult.Success) events.add(awaitItem())
            expectNoEvents()
        }
        return events
    }
}
