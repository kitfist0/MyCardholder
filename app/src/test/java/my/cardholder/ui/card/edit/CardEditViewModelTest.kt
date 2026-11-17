package my.cardholder.ui.card.edit

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import my.cardholder.data.CardRepository
import my.cardholder.data.CategoryRepository
import my.cardholder.data.model.Card
import my.cardholder.data.model.CardAndCategory
import my.cardholder.data.model.SupportedFormat
import my.cardholder.util.ImageUrlValidator
import my.cardholder.util.NetworkChecker
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardEditViewModelTest {

    private companion object {
        const val CARD_ID = 3L
    }

    private val cardRepository = mockk<CardRepository>(relaxed = true)
    private val cardFromDatabase = MutableStateFlow<CardAndCategory?>(null)
    private val categoryRepository = mockk<CategoryRepository>(relaxed = true)

    private val card = Card(
        id = CARD_ID,
        name = "Shop",
        position = 0,
        isPinned = false,
        content = "123",
        color = "#FFFFFF",
        format = SupportedFormat.QR_CODE,
        path = null,
        changedAt = 0L,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        cardFromDatabase.value = CardAndCategory(card, null)
        every { cardRepository.getCardAndCategory(CARD_ID) } returns cardFromDatabase
        coEvery { categoryRepository.getCategoryNames() } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `empty name shows error and is not saved`() = runTest {
        val viewModel = createViewModel()

        viewModel.onCardNameChanged("  ")

        coVerify(exactly = 0) { cardRepository.updateCardName(CARD_ID, "  ") }
        assertTrue((viewModel.state.value as CardEditState.Success).cardNameIsEmpty)
    }

    @Test
    fun `erased name brings back the name the card had when opened`() = runTest {
        val viewModel = createViewModel()

        // Erasing the name letter by letter saves each shorter version
        viewModel.onCardNameChanged("Sho")
        viewModel.onCardNameChanged("S")
        viewModel.onCardNameChanged("")

        coVerifyOrder {
            cardRepository.updateCardName(CARD_ID, "Sho")
            cardRepository.updateCardName(CARD_ID, "S")
            cardRepository.updateCardName(CARD_ID, "Shop")
        }
    }

    @Test
    fun `entered name is saved and hides error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onCardNameChanged("")

        viewModel.onCardNameChanged("New shop")

        coVerify { cardRepository.updateCardName(CARD_ID, "New shop") }
        assertFalse((viewModel.state.value as CardEditState.Success).cardNameIsEmpty)
    }

    @Test
    fun `typed name is the state of name field right away`() = runTest {
        val viewModel = createViewModel()

        viewModel.onCardNameChanged("Cof")

        assertEquals("Cof", successState(viewModel).cardName)
    }

    @Test
    fun `saved name coming back late does not replace name typed since`() = runTest {
        val viewModel = createViewModel()
        viewModel.onCardNameChanged("C")
        viewModel.onCardNameChanged("Cof")

        // The database sends the card saved with "C" while "Cof" is already typed
        cardFromDatabase.value = CardAndCategory(card.copy(name = "C", color = "#000000"), null)

        val state = successState(viewModel)
        assertEquals("Cof", state.cardName)
        assertEquals("Other fields still come from the database", "#000000", state.cardColor)
    }

    @Test
    fun `saved logo coming back late does not replace logo typed since`() = runTest {
        val viewModel = createViewModel()
        viewModel.onCardLogoChanged("shop")
        viewModel.onCardLogoChanged("shop.com")

        cardFromDatabase.value = CardAndCategory(card.copy(logo = "shop"), null)

        assertEquals("shop.com", successState(viewModel).cardLogo)
    }

    @Test
    fun `erased logo stays erased`() = runTest {
        cardFromDatabase.value = CardAndCategory(card.copy(logo = "shop.com"), null)
        val viewModel = createViewModel()

        viewModel.onCardLogoChanged("")
        cardFromDatabase.value = CardAndCategory(card.copy(logo = "shop.co"), null)

        assertEquals("", successState(viewModel).cardLogo)
        coVerify { cardRepository.updateCardLogo(CARD_ID, null) }
    }

    @Test
    fun `last typed name is saved last even if saving is slow`() = runTest(StandardTestDispatcher().also { Dispatchers.setMain(it) }) {
        val savedNames = mutableListOf<String>()
        // Earlier saves take longer, so if saves overlapped, an older name would be written last
        coEvery { cardRepository.updateCardName(CARD_ID, any()) } coAnswers {
            val name = secondArg<String>()
            delay(500L - name.length * 100L)
            savedNames += name
        }
        val viewModel = createViewModel()
        advanceUntilIdle()

        listOf("R", "Ra", "Rap", "Rapid").forEach { name ->
            viewModel.onCardNameChanged(name)
            delay(30)
        }
        advanceUntilIdle()

        assertEquals("Rapid", savedNames.last())
        assertEquals("Saves don't overlap, so they keep the typing order", savedNames.sortedBy { it.length }, savedNames)
    }

    private fun successState(viewModel: CardEditViewModel) = viewModel.state.value as CardEditState.Success

    private fun createViewModel() = CardEditViewModel(
        SavedStateHandle(mapOf("card_id" to CARD_ID)),
        cardRepository,
        categoryRepository,
        mockk<ImageUrlValidator>(relaxed = true),
        mockk<NetworkChecker>(relaxed = true),
    )
}
