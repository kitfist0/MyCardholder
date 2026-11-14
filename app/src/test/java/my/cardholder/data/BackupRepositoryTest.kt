package my.cardholder.data

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import my.cardholder.data.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalCoroutinesApi::class)
class BackupRepositoryTest {

    private val cardRepository = mockk<CardRepository>()
    private val categoryRepository = mockk<CategoryRepository>()
    private val backupRepository = BackupRepository(cardRepository, categoryRepository)

    @Test
    fun `success export to backup file`() = runTest {
        val card = Card(
            id = 1,
            name = "Test Card",
            position = 0,
            content = "Test Content",
            color = "#FFFFFF",
            format = SupportedFormat.QR_CODE,
            path = null,
            changedAt = 0,
            isPinned = false,
            logo = "test_logo",
            comment = "test_comment"
        )
        val category = Category(id = 1, name = "Test Category")
        val cardsAndCategories = listOf(CardAndCategory(card, category))

        every { cardRepository.cardsAndCategories } returns flowOf(cardsAndCategories)

        val outputStream = ByteArrayOutputStream()
        backupRepository.exportToBackupFile(outputStream).test {
            // Since there is only one card, it should emit Success directly (current == total)
            val result = awaitItem()
            assertTrue(result is BackupResult.Success)
            assertEquals(BackupOperationType.EXPORT, (result as BackupResult.Success).type)
            awaitComplete()
        }

        val writtenContent = outputStream.toString()
        assertTrue(writtenContent.startsWith(BackupRepository.CSV_SCHEME_VERSION.toString()))
        assertTrue(writtenContent.contains("Test Card"))
        assertTrue(writtenContent.contains("Test Category"))
        assertTrue(writtenContent.contains("Test Content"))
        assertTrue(writtenContent.contains("test_logo"))
        assertTrue(writtenContent.contains("test_comment"))
    }

    @Test
    fun `success import from backup file`() = runTest {
        // row: name, category, content, color, format, logo, position, comment
        val csvContent = "${BackupRepository.CSV_SCHEME_VERSION}\n\"Test Card\",\"Test Category\",\"Test Content\",\"#FFFFFF\",\"QR_CODE\",\"test_logo\",\"7\",\"test_comment\"\n"
        val inputStream = ByteArrayInputStream(csvContent.toByteArray())

        coEvery { cardRepository.isCardWithSuchDataExists(any(), any(), any()) } returns false
        coEvery { categoryRepository.upsertCategoryIfCategoryNameIsNew(categoryName = any()) } returns 10L
        coEvery { cardRepository.insertNewCard(any(), any(), any(), any(), any(), any(), any(), any()) } returns 1L

        backupRepository.importFromBackupFile(inputStream).test {
            val result = awaitItem()
            assertTrue(result is BackupResult.Success)
            assertEquals(BackupOperationType.IMPORT, (result as BackupResult.Success).type)
            awaitComplete()
        }

        coVerify {
            cardRepository.insertNewCard(
                name = "Test Card",
                position = 7,
                logo = "test_logo",
                categoryId = 10L,
                content = "Test Content",
                color = "#FFFFFF",
                comment = "test_comment",
                format = SupportedFormat.QR_CODE
            )
        }
    }

    @Test
    fun `backup of newer scheme version returns newer version error when importing`() = runTest {
        val csvContent = "${BackupRepository.CSV_SCHEME_VERSION + 1}\n\"Card\",\"Cat\",\"Cont\",\"Col\",\"QR_CODE\",\"\",\"0\",\"\"\n"

        assertImportError(csvContent, BackupError.NEWER_BACKUP_VERSION)
    }

    @Test
    fun `file of another kind returns not backup file error when importing`() = runTest {
        assertImportError("Name,Phone\nJohn,123\n", BackupError.NOT_BACKUP_FILE)
        assertImportError("0\n\"Card\",\"\",\"123\",\"#FFFFFF\",\"QR_CODE\"\n", BackupError.NOT_BACKUP_FILE)
        assertImportError("", BackupError.NOT_BACKUP_FILE)
    }

    @Test
    fun `backup without cards returns no cards error when importing`() = runTest {
        assertImportError("${BackupRepository.CSV_SCHEME_VERSION}\n", BackupError.NO_CARDS)
    }

    @Test
    fun `backup with only damaged cards returns damaged file error when importing`() = runTest {
        val csvContent = "${BackupRepository.CSV_SCHEME_VERSION}\n" +
                "\"Card\",\"\",\"123\",\"#FFFFFF\",\"UNKNOWN_FORMAT\",\"\",\"0\",\"\"\n"

        assertImportError(csvContent, BackupError.DAMAGED_BACKUP_FILE)
        coVerify(exactly = 0) { cardRepository.insertNewCard(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `damaged cards are skipped and counted when importing`() = runTest {
        val csvContent = "${BackupRepository.CSV_SCHEME_VERSION}\n" +
                "\"Good\",\"\",\"123\",\"#FFFFFF\",\"QR_CODE\",\"\",\"0\",\"\"\n" +
                "\"Unknown format\",\"\",\"456\",\"#FFFFFF\",\"UNKNOWN_FORMAT\",\"\",\"1\",\"\"\n" +
                "\"No content\",\"\",\"\",\"#FFFFFF\",\"QR_CODE\",\"\",\"2\",\"\"\n"
        coEvery { cardRepository.isCardWithSuchDataExists(any(), any(), any()) } returns false
        coEvery { cardRepository.insertNewCard(any(), any(), any(), any(), any(), any(), any(), any()) } returns 1L

        backupRepository.importFromBackupFile(csvContent.byteInputStream()).test {
            assertEquals(BackupResult.Success(BackupOperationType.IMPORT, numOfSkippedCards = 2), awaitItem())
            awaitComplete()
        }

        coVerify(exactly = 1) { cardRepository.insertNewCard(any(), any(), any(), any(), any(), any(), any(), any()) }
        coVerify { cardRepository.insertNewCard(name = "Good", any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `cards of first scheme version are imported`() = runTest {
        val csvContent = "1\n\"Old card\",\"\",\"123\",\"#FFFFFF\",\"EAN_13\"\n"
        coEvery { cardRepository.isCardWithSuchDataExists(any(), any(), any()) } returns false
        coEvery { cardRepository.insertNewCard(any(), any(), any(), any(), any(), any(), any(), any()) } returns 1L

        backupRepository.importFromBackupFile(csvContent.byteInputStream()).test {
            assertEquals(BackupResult.Success(BackupOperationType.IMPORT), awaitItem())
            awaitComplete()
        }

        coVerify {
            cardRepository.insertNewCard(
                name = "Old card",
                position = null,
                logo = null,
                categoryId = null,
                content = "123",
                color = "#FFFFFF",
                comment = null,
                format = SupportedFormat.EAN_13,
            )
        }
    }

    @Test
    fun `failed export returns operation failed error with details`() = runTest {
        every { cardRepository.cardsAndCategories } returns flowOf(listOf(CardAndCategory(card(), null)))
        val brokenOutputStream = object : java.io.OutputStream() {
            override fun write(b: Int) = throw java.io.IOException("Disk is full")
        }

        backupRepository.exportToBackupFile(brokenOutputStream).test {
            val result = awaitItem() as BackupResult.Error
            assertEquals(BackupError.OPERATION_FAILED, result.error)
            assertTrue(!result.details.isNullOrBlank())
            awaitComplete()
        }
    }

    private suspend fun assertImportError(csvContent: String, error: BackupError) {
        backupRepository.importFromBackupFile(csvContent.byteInputStream()).test {
            assertEquals(BackupResult.Error(error), awaitItem())
            awaitComplete()
        }
    }

    private fun card() = Card(
        id = 1,
        name = "Card",
        position = 0,
        content = "123",
        color = "#FFFFFF",
        format = SupportedFormat.QR_CODE,
        path = null,
        changedAt = 0,
        isPinned = false,
    )

    @Test
    fun `sync snapshot survives encoding and decoding`() = runTest {
        val snapshot = SyncSnapshot(
            cards = listOf(
                SyncCard(
                    syncId = "id-1",
                    changedAt = 100L,
                    name = "Card, with \"quotes\"",
                    categoryName = "Shops",
                    content = "123",
                    color = "#FFFFFF",
                    format = SupportedFormat.EAN_13,
                    logo = "logo",
                    position = 3,
                    comment = "multi\nline",
                ),
                SyncCard(
                    syncId = "id-2",
                    changedAt = 200L,
                    name = "Plain",
                    categoryName = null,
                    content = "456",
                    color = "#000000",
                    format = SupportedFormat.QR_CODE,
                    logo = null,
                    position = 0,
                    comment = null,
                ),
            ),
            deletedCards = listOf(DeletedCard(syncId = "id-3", deletedAt = 300L)),
        )

        val decoded = backupRepository.decodeSyncSnapshot(backupRepository.encodeSyncSnapshot(snapshot))

        assertEquals(snapshot, decoded)
    }

    @Test
    fun `legacy backup is decoded without sync data`() = runTest {
        val csvContent = "4\n\"Old\",\"Cat\",\"789\",\"#FFFFFF\",\"QR_CODE\",\"\",\"2\",\"\"\n"

        val decoded = backupRepository.decodeSyncSnapshot(csvContent)

        assertEquals(emptyList<DeletedCard>(), decoded.deletedCards)
        val card = decoded.cards.single()
        assertEquals(null, card.syncId)
        assertEquals(0L, card.changedAt)
        assertEquals("Old", card.name)
        assertEquals("Cat", card.categoryName)
        assertEquals(2, card.position)
        assertEquals(null, card.logo)
    }

    @Test(expected = Throwable::class)
    fun `backup of newer scheme version is not decoded`() = runTest {
        backupRepository.decodeSyncSnapshot("${BackupRepository.CSV_SCHEME_VERSION + 1}\n")
    }

    @Test
    fun `import from backup file skips deleted cards`() = runTest {
        val snapshot = SyncSnapshot(
            cards = listOf(
                SyncCard(
                    syncId = "id-1",
                    changedAt = 100L,
                    name = "Alive",
                    categoryName = null,
                    content = "123",
                    color = "#FFFFFF",
                    format = SupportedFormat.QR_CODE,
                    logo = null,
                    position = 0,
                    comment = null,
                ),
            ),
            deletedCards = listOf(DeletedCard(syncId = "id-2", deletedAt = 300L)),
        )
        val inputStream = ByteArrayInputStream(backupRepository.encodeSyncSnapshot(snapshot).toByteArray())
        coEvery { cardRepository.isCardWithSuchDataExists(any(), any(), any()) } returns false
        coEvery { cardRepository.insertNewCard(any(), any(), any(), any(), any(), any(), any(), any()) } returns 1L

        backupRepository.importFromBackupFile(inputStream).test {
            assertTrue(awaitItem() is BackupResult.Success)
            awaitComplete()
        }

        coVerify(exactly = 1) {
            cardRepository.insertNewCard(any(), any(), any(), any(), any(), any(), any(), any())
        }
        coVerify { cardRepository.insertNewCard(name = "Alive", any(), any(), any(), any(), any(), any(), any()) }
    }
}
