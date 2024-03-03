package my.cardholder.ui.card.action

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel

class CardActionViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        CardActionState(
            cardName = "",
            isPinned = false,
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            cardRepository.getCardAndCategory(cardId).first()?.let { cardAndCategory ->
                val card = cardAndCategory.card
                _state.value = CardActionState(cardName = card.name, isPinned = card.isPinned)
            }
        }
    }

    fun onPinUnpinButtonClicked() {
        viewModelScope.launch {
            cardRepository.pinUnpinCard(cardId, state.value.isPinned)
        }
        navigateUp()
    }

    fun onDeleteButtonClicked() {
        navigate(CardActionDialogDirections.fromCardActionToDeleteCard(cardId))
    }
}

@AssistedFactory
interface CardActionViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardActionViewModel
}
