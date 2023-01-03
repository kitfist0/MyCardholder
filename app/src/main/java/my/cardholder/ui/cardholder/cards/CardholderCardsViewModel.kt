package my.cardholder.ui.cardholder.cards

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardholderCardsViewModel @Inject constructor(
    cardRepository: CardRepository,
): BaseViewModel() {

    private val _state = MutableStateFlow<CardholderCardsState>(CardholderCardsState.Empty())
    val state = _state.asStateFlow()

    init {
        cardRepository.cards
            .onEach { cards ->
                _state.value = if (cards.isNotEmpty()) {
                    CardholderCardsState.Success(cards)
                } else {
                    CardholderCardsState.Empty()
                }
            }
            .launchIn(viewModelScope)
    }

    fun onCardClicked(cardId: Long, extras: Navigator.Extras) {
        navigate(CardholderCardsFragmentDirections.fromCardsToViewer(cardId), extras)
    }
}
