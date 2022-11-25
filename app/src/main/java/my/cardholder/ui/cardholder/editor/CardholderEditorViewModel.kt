package my.cardholder.ui.cardholder.editor

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.model.Card
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel

class CardholderEditorViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private companion object {
        const val CARD_DATA_MIN_UPDATE_INTERVAL_MILLIS = 300L
        const val CARD_DATA_UPDATED_LONG_TIME_AGO = -1L
    }

    private var lastCardDataUpdateTime = CARD_DATA_UPDATED_LONG_TIME_AGO
    private var updatedCardName: String? = null
    private var updatedCardText: String? = null
    private var updatedCardColor: String? = null

    val card: Flow<Card> = cardRepository.getCard(cardId).filterNotNull()
    val cardColors = flowOf(Card.COLORS.toList())

    fun onStop() {
        lastCardDataUpdateTime = CARD_DATA_UPDATED_LONG_TIME_AGO
        updateCardDataIfUpdatedLongTimeAgo()
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
        updateCardDataIfUpdatedLongTimeAgo()
    }

    fun onCardTextChanged(cardText: String?) {
        updatedCardText = cardText
        updateCardDataIfUpdatedLongTimeAgo()
    }

    private fun updateCardDataIfUpdatedLongTimeAgo() {
        val timeMillis = System.currentTimeMillis()
        if (timeMillis - lastCardDataUpdateTime < CARD_DATA_MIN_UPDATE_INTERVAL_MILLIS) {
            return
        }
        lastCardDataUpdateTime = timeMillis
        viewModelScope.launch {
            cardRepository.updateCardNameAndText(cardId, updatedCardName, updatedCardText)
        }
    }
}

@AssistedFactory
interface CardholderEditorViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardholderEditorViewModel
}
