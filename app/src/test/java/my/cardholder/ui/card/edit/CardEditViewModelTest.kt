package my.cardholder.ui.card.edit

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
        every { cardRepository.getCardAndCategory(CARD_ID) } returns flowOf(CardAndCategory(card, null))
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

    private fun createViewModel() = CardEditViewModel(
        SavedStateHandle(mapOf("card_id" to CARD_ID)),
        cardRepository,
        categoryRepository,
        mockk<ImageUrlValidator>(relaxed = true),
        mockk<NetworkChecker>(relaxed = true),
    )
}
