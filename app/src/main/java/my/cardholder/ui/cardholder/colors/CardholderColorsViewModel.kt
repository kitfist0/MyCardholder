package my.cardholder.ui.cardholder.colors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.data.model.Card

class CardholderColorsViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
) : ViewModel() {

    val colors = Card.COLORS.toList()

    fun onColorClicked(color: String) {
        viewModelScope.launch {
            cardRepository.updateCardColor(cardId, color)
        }
    }
}

@AssistedFactory
interface CardholderColorsViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardholderColorsViewModel
}
