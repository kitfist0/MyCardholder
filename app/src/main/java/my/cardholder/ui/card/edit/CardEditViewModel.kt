package my.cardholder.ui.card.edit

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.data.model.SupportedFormat
import my.cardholder.ui.base.BaseViewModel

class CardEditViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private var updatedCardName: String? = null
    private var updatedCardContent: String? = null
    private var updatedCardCategory: String? = null
    private var updatedCardColor: String? = null
    private var updatedCardFormat: SupportedFormat? = null

    private val _state = MutableStateFlow<CardEditState>(CardEditState.Loading)
    val state = _state.asStateFlow()

    init {
        cardRepository.getCardWithLabels(cardId)
            .filterNotNull()
            .onEach { cardWithLabels ->
                val card = cardWithLabels.card
                _state.value = CardEditState.Success(
                    barcodeFile = card.barcodeFile,
                    cardLabels = cardWithLabels.labels.map { it.text },
                    cardName = card.name,
                    cardContent = card.content,
                    barcodeFormatName = card.format.toString(),
                    cardColor = card.color,
                )
                if (card.barcodeFile?.exists() == false) {
                    showSnack("The value is invalid for the selected barcode type")
                }
            }
            .launchIn(viewModelScope)
    }

    fun onOkFabClicked() {
        navigateUp()
    }

    fun onCardNameChanged(cardName: String?) {
        updatedCardName = cardName
        updateCardData()
    }

    fun onCardContentChanged(cardContent: String?) {
        updatedCardContent = cardContent
        updateCardData()
    }

    fun onCardCategoryChanged(cardCategory: String?) {
        if (cardCategory == null || updatedCardCategory == cardCategory) {
            return
        }
        updatedCardCategory = cardCategory
        updateCardData()
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
        updateCardData()
    }

    private fun updateCardData() {
        viewModelScope.launch {
            cardRepository.updateCardDataIfItChanges(
                id = cardId,
                name = updatedCardName,
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
