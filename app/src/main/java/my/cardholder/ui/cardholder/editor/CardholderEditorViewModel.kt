package my.cardholder.ui.cardholder.editor

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.ui.base.BaseViewModel

class CardholderEditorViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private var updatedCardName: String? = null
    private var updatedCardText: String? = null
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

    fun onCardTextChanged(cardText: String?) {
        updatedCardText = cardText
        updateCardData()
    }

    private fun updateCardData() {
        viewModelScope.launch {
            cardRepository.updateCardDataIfItChanges(cardId, updatedCardName, updatedCardText)
        }
    }
}

@AssistedFactory
interface CardholderEditorViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardholderEditorViewModel
}
