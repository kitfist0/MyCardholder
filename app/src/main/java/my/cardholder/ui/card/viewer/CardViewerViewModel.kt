package my.cardholder.ui.card.viewer

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import my.cardholder.data.model.Card
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel

class CardViewerViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    val card: Flow<Card> = cardRepository.getCard(cardId)

    fun onEditFabClicked(extras: Navigator.Extras) {
        navigate(CardViewerFragmentDirections.fromCardViewerToCardEditor(cardId), extras)
    }

    fun onDeleteCardButtonClicked() {
        viewModelScope.launch {
            cardRepository.deleteCard(cardId)
            navigateBack()
        }
    }
}

@AssistedFactory
interface CardViewerViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardViewerViewModel
}
