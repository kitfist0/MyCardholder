package my.cardholder.ui.card.edit

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.R
import my.cardholder.data.CardRepository
import my.cardholder.data.CategoryRepository
import my.cardholder.data.model.Category
import my.cardholder.data.model.SupportedFormat
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.Text

class CardEditViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
    private val categoryRepository: CategoryRepository,
) : BaseViewModel() {

    private var cardName: String? = null
    private var cardCategoryName: String? = null
    private var cardColor: String? = null
    private var cardFormat: String? = null

    private val _state = MutableStateFlow<CardEditState>(CardEditState.Loading)
    val state = _state.asStateFlow()

    init {
        cardRepository.getCardAndCategory(cardId)
            .filterNotNull()
            .onEach { cardAndCategory ->
                val categoryNames = categoryRepository.getCategoryNames()
                val card = cardAndCategory.card
                _state.value = CardEditState.Success(
                    barcodeFile = card.barcodeFile,
                    cardName = card.name,
                    cardContent = card.content,
                    cardCategoryName = cardAndCategory.category?.name ?: Category.NULL_NAME,
                    cardCategoryNames = listOf(Category.NULL_NAME).plus(categoryNames),
                    barcodeFormatName = card.format.toString(),
                    cardColor = card.color,
                )
                if (card.barcodeFile?.exists() == false) {
                    showSnack(Text.Resource(R.string.card_edit_invalid_value_error_message))
                }
            }
            .launchIn(viewModelScope)
    }

    fun onDeleteCardButtonClicked() {
        navigate(CardEditFragmentDirections.fromCardEditToDeleteCard(cardId))
    }

    fun onOkFabClicked() {
        navigateUp()
    }

    fun onCardNameChanged(changedName: String?) {
        if (changedName == null || cardName == changedName) {
            return
        }
        cardName = changedName
        viewModelScope.launch {
            cardRepository.updateCardName(cardId, changedName)
        }
    }

    fun onCardContentClicked(extras: Navigator.Extras) {
        navigate(CardEditFragmentDirections.fromCardEditToCardContent(cardId), extras)
    }

    fun onCardCategoryNameChanged(changedCategoryName: String?) {
        if (changedCategoryName == null || cardCategoryName == changedCategoryName) {
            return
        }
        cardCategoryName = changedCategoryName
        viewModelScope.launch {
            val categoryId = categoryRepository.getCategoryIdByName(changedCategoryName)
            cardRepository.updateCardCategoryId(cardId, categoryId)
        }
    }

    fun onCardColorChanged(changedColor: String?) {
        if (changedColor == null || cardColor == changedColor) {
            return
        }
        cardColor = changedColor
        viewModelScope.launch {
            cardRepository.updateCardColor(cardId, changedColor)
        }
    }

    fun onCardFormatChanged(changedFormat: String?) {
        if (changedFormat == null || cardFormat == changedFormat) {
            return
        }
        cardFormat = changedFormat
        viewModelScope.launch {
            cardRepository.updateCardFormat(cardId, SupportedFormat.valueOf(changedFormat))
        }
    }
}

@AssistedFactory
interface CardEditViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardEditViewModel
}
