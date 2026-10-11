package my.cardholder.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import my.cardholder.data.model.Card
import my.cardholder.data.model.SupportedFormat
import my.cardholder.data.model.WidgetCard
import my.cardholder.data.source.WidgetCardDao
import org.junit.Assert.assertEquals
import org.junit.Test

class CardWidgetRepositoryTest {

    private val widgetCardDao = mockk<WidgetCardDao>(relaxed = true)
    private val cardWidgetRepository = CardWidgetRepository(widgetCardDao)

    @Test
    fun `getWidgetCards returns cards of the widget from dao`() = runTest {
        val cards = listOf(card(1), card(2))
        coEvery { widgetCardDao.getWidgetCards(7) } returns cards

        assertEquals(cards, cardWidgetRepository.getWidgetCards(7))
    }

    @Test
    fun `getWidgetCardIds returns card ids of the widget as a set`() = runTest {
        coEvery { widgetCardDao.getWidgetCardIds(7) } returns listOf(3L, 1L)

        assertEquals(setOf(1L, 3L), cardWidgetRepository.getWidgetCardIds(7))
    }

    @Test
    fun `setWidgetCards replaces cards of the widget`() = runTest {
        cardWidgetRepository.setWidgetCards(7, setOf(1L, 2L))

        coVerify {
            widgetCardDao.replaceWidgetCards(7, listOf(WidgetCard(7, 1L), WidgetCard(7, 2L)))
        }
    }

    @Test
    fun `deleteWidgets deletes cards of the widgets`() = runTest {
        cardWidgetRepository.deleteWidgets(listOf(7, 8))

        coVerify { widgetCardDao.deleteWidgets(listOf(7, 8)) }
    }

    private fun card(id: Long) = Card(
        id = id,
        name = "Card $id",
        position = id.toInt(),
        isPinned = false,
        content = "content",
        color = "#FFFFFF",
        format = SupportedFormat.QR_CODE,
        path = null,
        changedAt = 0L,
    )
}
