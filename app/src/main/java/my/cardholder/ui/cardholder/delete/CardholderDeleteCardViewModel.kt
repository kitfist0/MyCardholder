package my.cardholder.ui.cardholder.delete

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel

class CardholderDeleteCardViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
) : BaseViewModel() {
    fun onDeleteButtonClicked() {
        viewModelScope.launch {
            cardRepository.deleteCard(cardId)
            navigate(CardholderDeleteCardDialogDirections.fromDeleteCardToCards())
        }
    }
}

@AssistedFactory
interface CardholderDeleteCardViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardholderDeleteCardViewModel
}
