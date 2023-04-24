package my.cardholder.ui.card.display

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import my.cardholder.data.CardRepository
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.ui.base.BaseViewModel

class CardDisplayViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    cardRepository: CardRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow<CardDisplayState>(CardDisplayState.Loading)
    val state = _state.asStateFlow()

    init {
        cardRepository.getCard(cardId)
            .filterNotNull()
            .onEach { card ->
                _state.value = CardDisplayState.Success(
                    cardName = card.name,
                    cardContent = card.content,
                    cardColor = card.getColorInt(),
                    barcodeFile = card.barcodeFile,
                )
            }
            .launchIn(viewModelScope)
    }

    fun onEditFabClicked(extras: Navigator.Extras) {
        navigate(CardDisplayFragmentDirections.fromCardDisplayToCardEdit(cardId), extras)
    }

    fun onDeleteCardButtonClicked() {
        navigate(CardDisplayFragmentDirections.fromCardDisplayToDeleteCard(cardId))
    }
}

@AssistedFactory
interface CardDisplayViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardDisplayViewModel
}
