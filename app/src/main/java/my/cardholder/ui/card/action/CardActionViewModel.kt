package my.cardholder.ui.card.action

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardActionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private val cardId = CardActionDialogArgs.fromSavedStateHandle(savedStateHandle).cardId

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
        navigateUp()
    }

    fun onDeleteButtonClicked() {
        navigate(CardActionDialogDirections.fromCardActionToDeleteCard(cardId))
    }
}
