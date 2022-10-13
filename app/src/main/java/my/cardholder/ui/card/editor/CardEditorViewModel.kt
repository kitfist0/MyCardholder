package my.cardholder.ui.card.editor

import androidx.lifecycle.asFlow
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import my.cardholder.data.model.Card
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel

class CardEditorViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private var updatedCardName: String? = null
    private var updatedCardText: String? = null

    private val _card = liveData {
        emit(cardRepository.getCard(cardId))
    }
    val card: Flow<Card> = _card.asFlow()

    fun onOkFabClicked() {
        when {
            updatedCardName.isNullOrEmpty() -> showSnack("Empty card name!")
            updatedCardText.isNullOrEmpty() -> showSnack("Empty card text!")
            else -> viewModelScope.launch {
                cardRepository.updateCard(cardId, updatedCardName, updatedCardText)
                navigateBack()
            }
        }
    }

    fun onCardNameChanged(cardName: String?) {
        updatedCardName = cardName
    }

    fun onCardTextChanged(cardText: String?) {
        updatedCardText = cardText
    }
}

@AssistedFactory
interface CardEditorViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardEditorViewModel
}
