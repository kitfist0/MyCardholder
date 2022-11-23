package my.cardholder.ui.cardholder.viewer

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import my.cardholder.data.model.Card
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel

class CardholderViewerViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    val card: Flow<Card> = cardRepository.getCard(cardId).filterNotNull()

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
