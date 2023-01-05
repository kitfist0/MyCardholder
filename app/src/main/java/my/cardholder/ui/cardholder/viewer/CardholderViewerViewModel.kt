package my.cardholder.ui.cardholder.viewer

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel

class CardholderViewerViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow<CardholderViewerState>(CardholderViewerState.Loading)
    val state = _state.asStateFlow()

    init {
        cardRepository.getCard(cardId)
            .filterNotNull()
            .onEach { card -> _state.value = CardholderViewerState.Success(card) }
            .launchIn(viewModelScope)
    }

    fun onEditFabClicked(extras: Navigator.Extras) {
        navigate(CardholderViewerFragmentDirections.fromViewerToEditor(cardId), extras)
    }

    fun onDeleteCardButtonClicked() {
        viewModelScope.launch {
            cardRepository.deleteCard(cardId)
            navigateUp()
        }
    }
}

@AssistedFactory
interface CardholderViewerViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardholderViewerViewModel
}
