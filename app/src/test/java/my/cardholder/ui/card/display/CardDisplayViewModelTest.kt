package my.cardholder.ui.card.display

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import my.cardholder.R
import my.cardholder.data.CardRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.Card
import my.cardholder.data.model.CardAndCategory
import my.cardholder.data.model.SupportedFormat
import my.cardholder.shortcut.CardShortcutManager
import my.cardholder.ui.base.BaseEvent
import my.cardholder.util.Text
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardDisplayViewModelTest {

    private companion object {
        const val CARD_ID = 3L
    }

    private val cardRepository = mockk<CardRepository>()
    private val cardShortcutManager = mockk<CardShortcutManager>()
    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)

    private val card = Card(
        id = CARD_ID,
        name = "Card",
        position = 0,
        isPinned = false,
        content = "content",
        color = "#FFFFFF",
        format = SupportedFormat.QR_CODE,
        path = null,
        changedAt = 0L,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { cardRepository.getCardAndCategory(CARD_ID) } returns flowOf(CardAndCategory(card, null))
        every { settingsRepository.explanationAboutBarcodeZoomIsRequired } returns flowOf(false)
        coEvery { cardRepository.getCard(CARD_ID) } returns card
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `shortcut menu item requests pinning the card shortcut`() = runTest {
        coEvery { cardShortcutManager.requestPinShortcut(card) } returns true
        val viewModel = createViewModel()

        viewModel.baseEvents.test {
            viewModel.onToolbarMenuItemClicked(R.id.card_shortcut_menu_item)

            coVerify { cardShortcutManager.requestPinShortcut(card) }
            expectNoEvents()
        }
    }

    @Test
    fun `shortcut menu item shows snack when pinning is not supported`() = runTest {
        coEvery { cardShortcutManager.requestPinShortcut(card) } returns false
        val viewModel = createViewModel()

        viewModel.baseEvents.test {
            viewModel.onToolbarMenuItemClicked(R.id.card_shortcut_menu_item)

            assertEquals(
                BaseEvent.SnackMessage(Text.Resource(R.string.card_display_shortcut_not_supported_message)),
                awaitItem(),
            )
        }
    }

    @Test
    fun `shortcut menu item does nothing for a missing card`() = runTest {
        coEvery { cardRepository.getCard(CARD_ID) } returns null
        val viewModel = createViewModel()

        viewModel.onToolbarMenuItemClicked(R.id.card_shortcut_menu_item)

        coVerify(exactly = 0) { cardShortcutManager.requestPinShortcut(any()) }
    }

    @Test
    fun `missing card shows toast and navigates up`() = runTest {
        every { cardRepository.getCardAndCategory(CARD_ID) } returns flowOf(null)

        createViewModel().baseEvents.test {
            assertEquals(
                BaseEvent.ToastMessage(Text.Resource(R.string.shortcut_card_removed_message)),
                awaitItem(),
            )
            assertEquals(BaseEvent.NavigateUp, awaitItem())
        }
    }

    @Test
    fun `card deleted while shown keeps its data and does not navigate`() = runTest {
        val cardAndCategory = MutableStateFlow<CardAndCategory?>(CardAndCategory(card, null))
        every { cardRepository.getCardAndCategory(CARD_ID) } returns cardAndCategory
        val viewModel = createViewModel()

        viewModel.baseEvents.test {
            cardAndCategory.value = null

            assertEquals("Card", (viewModel.state.value as CardDisplayState.Success).cardName)
            expectNoEvents()
        }
    }

    private fun createViewModel() = CardDisplayViewModel(
        SavedStateHandle(mapOf("card_id" to CARD_ID)),
        cardRepository,
        cardShortcutManager,
        settingsRepository,
    )
}
