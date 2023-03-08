package my.cardholder.ui.cardholder.editor

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.data.model.SupportedFormat
import my.cardholder.ui.base.BaseViewModel

class CardholderEditorViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private var updatedCardName: String? = null
    private var updatedCardContent: String? = null
    private var updatedCardFormat: SupportedFormat? = null
    private var updatedCardColor: String? = null

    private val _state = MutableStateFlow<CardholderEditorState>(CardholderEditorState.Loading)
    val state = _state.asStateFlow()

    init {
        cardRepository.getCard(cardId)
            .filterNotNull()
            .onEach { card ->
                _state.value = CardholderEditorState.Success(
                    cardName = card.name,
                    cardContent = card.content,
                    cardColor = card.getColorInt(),
                    barcodeFileName = card.barcodeFileName,
                    barcodeFormatName = card.format.toString(),
                )
                if (!cardRepository.isCardBarcodeFileExist(card)) {
                    showSnack("The value is invalid for the selected barcode type")
                }
            }
            .launchIn(viewModelScope)
    }

    fun onOkFabClicked() {
        navigateUp()
    }

    fun onColorItemClicked(color: String) {
        if (updatedCardColor == color) {
            return
        }
        viewModelScope.launch {
            cardRepository.updateCardColor(cardId, color)
        }
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
interface CardholderEditorViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardholderEditorViewModel
}
