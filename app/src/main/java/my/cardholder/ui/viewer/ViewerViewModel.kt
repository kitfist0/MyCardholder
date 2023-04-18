package my.cardholder.ui.viewer

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import my.cardholder.data.CardRepository
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.ui.base.BaseViewModel

class CardholderViewerViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    cardRepository: CardRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow<ViewerState>(ViewerState.Loading)
    val state = _state.asStateFlow()

    init {
        cardRepository.getCard(cardId)
            .filterNotNull()
            .onEach { card ->
                _state.value = ViewerState.Success(
                    cardName = card.name,
                    cardContent = card.content,
                    cardColor = card.getColorInt(),
                    barcodeFile = card.barcodeFile,
                )
            }
            .launchIn(viewModelScope)
    }

    fun onEditFabClicked(extras: Navigator.Extras) {
        navigate(ViewerFragmentDirections.fromViewerToEditor(cardId), extras)
    }

    fun onDeleteCardButtonClicked() {
        navigate(ViewerFragmentDirections.fromViewerToDeleteCard(cardId))
    }
}

@AssistedFactory
interface ViewerViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardholderViewerViewModel
}
