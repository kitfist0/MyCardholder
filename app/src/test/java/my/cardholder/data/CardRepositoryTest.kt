package my.cardholder.data

import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import my.cardholder.data.model.Card
import my.cardholder.data.model.SupportedFormat
import my.cardholder.data.source.CardDao
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardRepositoryTest {

    private val barcodeFileRepository = mockk<BarcodeFileRepository>(relaxed = true)
    private val cardDao = mockk<CardDao>(relaxed = true)
    private val cardRepository = CardRepository(barcodeFileRepository, cardDao)

    @Test
    fun `insertNewCard writes barcode file and upserts card`() = runTest {
        val content = "12345"
        val format = SupportedFormat.QR_CODE
        val barcodePath = "path/to/barcode"

        every { barcodeFileRepository.writeBarcodeFile(content, format) } returns barcodePath
        coEvery { cardDao.getNumberOfCards() } returns 5
        coEvery { cardDao.upsert(any<Card>()) } returns 1L

        val cardId = cardRepository.insertNewCard(content = content, format = format)

        assertEquals(1L, cardId)
        verify { barcodeFileRepository.writeBarcodeFile(content, format) }
        coVerify {
            cardDao.upsert(match<Card> {
                it.content == content && it.format == format && it.path == barcodePath && it.position == 5
            })
        }
    }

    @Test
    fun `deleteCard deletes barcode file and card from database`() = runTest {
        val cardId = 1L
        val realCard = Card(
            id = cardId,
            name = "Test",
            position = 0,
            isPinned = false,
            content = "123",
            color = "#000000",
            format = SupportedFormat.QR_CODE,
            path = null,
            changedAt = 0
        )

        coEvery { cardDao.getCard(cardId) } returns realCard

        cardRepository.deleteCard(cardId)

        coVerify { cardDao.deleteCard(cardId) }
    }

    @Test
    fun `updateCardContent deletes old file, writes new one and upserts`() = runTest {
        val cardId = 1L
        val oldContent = "old"
        val newContent = "new"
        val format = SupportedFormat.QR_CODE
        val oldCard = Card(
            id = cardId,
            name = "Test",
            position = 0,
            isPinned = false,
            content = oldContent,
            color = Card.COLORS.random(),
            format = format,
            path = "old/path",
            changedAt = 0
        )

        coEvery { cardDao.getCard(cardId) } returns oldCard
        every { barcodeFileRepository.writeBarcodeFile(newContent, format) } returns "new/path"

        cardRepository.updateCardContent(cardId, newContent)

        verify { barcodeFileRepository.writeBarcodeFile(newContent, format) }
        coVerify {
            cardDao.upsert(match<Card> {
                it.content == newContent && it.path == "new/path"
            })
        }
    }

    @Test
    fun `updateCardPositions only upserts changed cards`() = runTest {
        val card0 = Card(
            id = 10,
            name = "C10",
            position = 0,
            isPinned = false,
            content = "",
            color = "",
            format = SupportedFormat.QR_CODE,
            path = null,
            changedAt = 0
        )
        val card1 = Card(
            id = 11,
            name = "C11",
            position = 2,
            isPinned = false,
            content = "",
            color = "",
            format = SupportedFormat.QR_CODE,
            path = null,
            changedAt = 0
        )
        val card2 = Card(
            id = 12,
            name = "C12",
            position = 1,
            isPinned = false,
            content = "",
            color = "",
            format = SupportedFormat.QR_CODE,
            path = null,
            changedAt = 0
        )

        val rightOrderedCards = listOf(card0, card1, card2)

        cardRepository.updateCardPositions(rightOrderedCards)

        coVerify {
            cardDao.upsert(match<List<Card>> { list ->
                list.size == 2 && list.any { it.id == 11L && it.position == 1 } && list.any { it.id == 12L && it.position == 2 }
            })
        }
    }

    @Test
    fun `searchCardsBy with categoryId calls specific dao method`() = runTest {
        val name = "query"
        val categoryId = 5L

        cardRepository.searchCardsBy(name, categoryId)

        coVerify { cardDao.getCardsWithCategoryIdAndWithNamesLike(categoryId, "%query%") }
    }

    @Test
    fun `searchCardsBy without categoryId calls generic dao method`() = runTest {
        val name = "query"

        cardRepository.searchCardsBy(name)

        coVerify { cardDao.getCardsWithNamesLike("%query%") }
    }
}
