package my.cardholder.widget

import android.appwidget.AppWidgetManager
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import my.cardholder.data.CardRepository
import my.cardholder.data.CardWidgetRepository
import my.cardholder.data.model.Card
import my.cardholder.data.model.CardAndCategory
import my.cardholder.data.model.SupportedFormat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardWidgetConfigureViewModelTest {

    private companion object {
        const val WIDGET_ID = 7
    }

    private val cardRepository = mockk<CardRepository>()
    private val cardWidgetRepository = mockk<CardWidgetRepository>(relaxed = true)
    private val cardWidgetManager = mockk<CardWidgetManager>(relaxed = true)

    private val cardsAndCategories = MutableStateFlow(listOf(cardAndCategory(1), cardAndCategory(2)))

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { cardRepository.cardsAndCategories } returns cardsAndCategories
        coEvery { cardWidgetRepository.getWidgetCardIds(WIDGET_ID) } returns emptySet()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `widget id is taken from the launcher intent`() {
        assertEquals(WIDGET_ID, createViewModel().widgetId)
    }

    @Test
    fun `missing widget id is invalid`() {
        val viewModel = CardWidgetConfigureViewModel(
            SavedStateHandle(), cardRepository, cardWidgetRepository, cardWidgetManager
        )

        assertEquals(AppWidgetManager.INVALID_APPWIDGET_ID, viewModel.widgetId)
    }

    @Test
    fun `state lists all cards with the ones the widget already shows selected`() = runTest {
        coEvery { cardWidgetRepository.getWidgetCardIds(WIDGET_ID) } returns setOf(2L)
        val viewModel = createViewModel()

        viewModel.state.test {
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertEquals(listOf(1L to false, 2L to true), state.items.map { it.card.id to it.isSelected })
            assertTrue(state.isSaveEnabled)
        }
    }

    @Test
    fun `save is disabled while no card is selected`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            assertFalse(expectMostRecentItem().isSaveEnabled)
        }
    }

    @Test
    fun `card click toggles its selection`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            viewModel.onCardClicked(1L)
            assertEquals(setOf(1L), expectMostRecentItem().selectedIds())
            viewModel.onCardClicked(1L)
            assertEquals(emptySet<Long>(), expectMostRecentItem().selectedIds())
        }
    }

    @Test
    fun `save stores selected cards, updates the widget and finishes`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            viewModel.onCardClicked(2L)
            expectMostRecentItem()
            viewModel.savedEvent.test {
                viewModel.onSaveClicked()
                awaitItem()
            }
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { cardWidgetRepository.setWidgetCards(WIDGET_ID, setOf(2L)) }
        verify { cardWidgetManager.updateWidgets(match { it.contentEquals(intArrayOf(WIDGET_ID)) }) }
    }

    @Test
    fun `save keeps only cards that still exist`() = runTest {
        coEvery { cardWidgetRepository.getWidgetCardIds(WIDGET_ID) } returns setOf(1L, 99L)
        val viewModel = createViewModel()

        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onSaveClicked()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { cardWidgetRepository.setWidgetCards(WIDGET_ID, setOf(1L)) }
    }

    @Test
    fun `save does nothing without selected cards`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onSaveClicked()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { cardWidgetRepository.setWidgetCards(any(), any()) }
    }

    private fun createViewModel() = CardWidgetConfigureViewModel(
        SavedStateHandle(mapOf(AppWidgetManager.EXTRA_APPWIDGET_ID to WIDGET_ID)),
        cardRepository,
        cardWidgetRepository,
        cardWidgetManager,
    )

    private fun CardWidgetConfigureState.selectedIds() = items.filter { it.isSelected }.map { it.card.id }.toSet()

    private fun cardAndCategory(id: Long) = CardAndCategory(
        card = Card(
            id = id,
            name = "Card $id",
            position = id.toInt(),
            isPinned = false,
            content = "content",
            color = "#FFFFFF",
            format = SupportedFormat.QR_CODE,
            path = null,
            changedAt = 0L,
        ),
        category = null,
    )
}
