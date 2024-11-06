package my.cardholder.ui.card.display

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import my.cardholder.data.CardRepository
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardDisplayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    cardRepository: CardRepository,
) : BaseViewModel() {

    private val cardId = CardDisplayFragmentArgs.fromSavedStateHandle(savedStateHandle).cardId

    private val _state = MutableStateFlow<CardDisplayState>(CardDisplayState.Loading)
    val state = _state.asStateFlow()

    init {
        cardRepository.getCardAndCategory(cardId)
            .filterNotNull()
            .onEach { cardAndCategory ->
                val card = cardAndCategory.card
                _state.value = CardDisplayState.Success(
                    barcodeFile = card.barcodeFile,
                    cardCategory = cardAndCategory.category?.name.orEmpty(),
                    cardName = card.name,
                    cardContent = card.content,
                    cardColor = card.getColorInt(),
                )
            }
            .launchIn(viewModelScope)
    }

    fun onBarcodeImageClicked(extras: Navigator.Extras) {
        navigate(CardDisplayFragmentDirections.fromCardDisplayToCardZoom(cardId), extras)
    }

    fun onCardContentTextLongClicked(extras: Navigator.Extras): Boolean {
        navigate(CardDisplayFragmentDirections.fromCardDisplayToCardContent(cardId), extras)
        return true
    }

    fun onEditFabClicked(extras: Navigator.Extras) {
        navigate(CardDisplayFragmentDirections.fromCardDisplayToCardEdit(cardId), extras)
    }
}
