package my.cardholder.ui.card.delete

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.shortcut.CardShortcutManager
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class DeleteCardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
    private val cardShortcutManager: CardShortcutManager,
) : BaseViewModel() {

    private val cardId = DeleteCardDialogArgs.fromSavedStateHandle(savedStateHandle).cardId

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
            cardShortcutManager.disableShortcut(cardId)
            navigate(DeleteCardDialogDirections.fromDeleteCardToCardList())
        }
    }
}
