package my.cardholder.ui.card.edit

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.data.model.SupportedFormat
import my.cardholder.data.source.CategoryDao
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.Text

class CardEditViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
    private val categoryDao: CategoryDao,
) : BaseViewModel() {

    private companion object {
        const val NO_CATEGORY_NAME_TEXT = "No category"
    }

    private var updatedCardName: String? = null
    private var updatedCardContent: String? = null
    private var updatedCardCategoryName: String? = null
    private var updatedCardColor: String? = null
    private var updatedCardFormat: SupportedFormat? = null

    private val _state = MutableStateFlow<CardEditState>(CardEditState.Loading)
    val state = _state.asStateFlow()

    init {
        cardRepository.getCardAndCategory(cardId)
            .filterNotNull()
            .onEach { cardAndCategory ->
                val categoryNames = categoryDao.getCategoryNames()
                val card = cardAndCategory.card
                _state.value = CardEditState.Success(
                    barcodeFile = card.barcodeFile,
                    cardName = card.name,
                    cardContent = card.content,
                    cardCategoryName = cardAndCategory.category?.name ?: NO_CATEGORY_NAME_TEXT,
                    cardCategoryNames = listOf(NO_CATEGORY_NAME_TEXT).plus(categoryNames),
                    barcodeFormatName = card.format.toString(),
                    cardColor = card.color,
                )
                if (card.barcodeFile?.exists() == false) {
                    showSnack(Text.Simple("The value is invalid for the selected barcode type"))
                }
            }
            .launchIn(viewModelScope)
    }

    fun onOkFabClicked() {
        navigateUp()
    }

    fun onCardNameChanged(cardName: String?) {
        if (cardName == null || updatedCardName == cardName) {
            return
        }
        updatedCardName = cardName
        viewModelScope.launch {
            cardRepository.updateCardName(cardId, cardName)
        }
    }

    fun onCardContentChanged(cardContent: String?) {
        updatedCardContent = cardContent
        updateCardBarcodeIfRequired()
    }

    fun onCardCategoryNameChanged(cardCategoryName: String?) {
        if (cardCategoryName == null || updatedCardCategoryName == cardCategoryName) {
            return
        }
        updatedCardCategoryName = cardCategoryName
        viewModelScope.launch {
            val category = if (cardCategoryName != NO_CATEGORY_NAME_TEXT) {
                categoryDao.getCategoryByName(cardCategoryName)
            } else {
                null
            }
            cardRepository.updateCardCategoryId(cardId, category?.id)
        }
    }

    fun onCardColorChanged(cardColor: String?) {
        if (cardColor == null || updatedCardColor == cardColor) {
            return
        }
        updatedCardColor = cardColor
        viewModelScope.launch {
            cardRepository.updateCardColor(cardId, cardColor)
        }
    }

    fun onCardFormatChanged(cardFormat: String?) {
        updatedCardFormat = cardFormat?.let { SupportedFormat.valueOf(it) }
        updateCardBarcodeIfRequired()
    }

    private fun updateCardBarcodeIfRequired() {
        viewModelScope.launch {
            cardRepository.updateCardBarcodeIfRequired(
                cardId = cardId,
                content = updatedCardContent,
                format = updatedCardFormat,
            )
        }
    }
}

@AssistedFactory
interface CardEditViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardEditViewModel
}
