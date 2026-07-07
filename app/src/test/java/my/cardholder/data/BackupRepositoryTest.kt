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
    fun `invalid csv scheme version returns error when importing from backup`() = runTest {
        val invalidVer = Int.MAX_VALUE
        val csvContent = "$invalidVer\n\"Card\",\"Cat\",\"Cont\",\"Col\",\"QR_CODE\",\"\",\"0\",\"\"\n"
        val inputStream = ByteArrayInputStream(csvContent.toByteArray())

        backupRepository.importFromBackupFile(inputStream).test {
            val result = awaitItem()
            assertTrue(result is BackupResult.Error)
            assertEquals("Invalid file format", (result as BackupResult.Error).message)
            awaitComplete()
        }
    }
}
