package my.cardholder.ui.cardholder.editor

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import my.cardholder.data.model.Card
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel

class CardholderEditorViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private var updatedCardName: String? = null
    private var updatedCardText: String? = null

    val card: Flow<Card> = cardRepository.getCard(cardId)

    fun onOkFabClicked() {
        when {
            updatedCardName.isNullOrEmpty() -> showSnack("Empty card name!")
            updatedCardText.isNullOrEmpty() -> showSnack("Empty card text!")
            else -> viewModelScope.launch {
                cardRepository.updateCardNameAndText(cardId, updatedCardName, updatedCardText)
                navigateBack()
            }
        }
    }

    fun onColorPickerButtonClicked() {
        navigate(CardholderEditorFragmentDirections.fromEditorToColors(cardId))
    }

    fun onCardNameChanged(cardName: String?) {
        updatedCardName = cardName
    }

    fun onCardTextChanged(cardText: String?) {
        updatedCardText = cardText
    }
}

@AssistedFactory
interface CardholderEditorViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardholderEditorViewModel
}
