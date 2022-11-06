package my.cardholder.ui.cardholder.cards

import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import my.cardholder.data.model.Card
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardholderCardsViewModel @Inject constructor(
    cardRepository: CardRepository,
): BaseViewModel() {

    val cards: Flow<List<Card>> = cardRepository.cards

    fun onCardClicked(cardId: Long, extras: Navigator.Extras) {
        navigate(CardholderCardsFragmentDirections.fromCardsToViewer(cardId), extras)
    }
}
