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
    private var updatedCardFormat: SupportedFormat? = null
    private var updatedCardColor: String? = null

    private val _state = MutableStateFlow<CardEditState>(CardEditState.Loading)
    val state = _state.asStateFlow()

    init {
        cardRepository.getCard(cardId)
            .filterNotNull()
            .onEach { card ->
                _state.value = CardEditState.Success(
                    barcodeFile = card.barcodeFile,
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

    fun onCardFormatChanged(cardFormat: String?) {
        updatedCardFormat = cardFormat?.let { SupportedFormat.valueOf(it) }
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
