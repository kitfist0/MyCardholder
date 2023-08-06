package my.cardholder.ui.card.delete

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel

class DeleteCardViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        DeleteCardState(cardName = "")
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            cardRepository.getCardAndCategory(cardId).first()?.let { cardAndCategory ->
                _state.value = DeleteCardState(cardName = cardAndCategory.card.name)
            }
        }
    }

    fun onDeleteConfirmationButtonClicked() {
        viewModelScope.launch {
            cardRepository.deleteCard(cardId)
            navigate(DeleteCardDialogDirections.fromDeleteCardToCardList())
        }
    }
}

@AssistedFactory
interface DeleteCardViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): DeleteCardViewModel
}
