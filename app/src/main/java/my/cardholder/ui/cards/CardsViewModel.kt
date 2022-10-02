package my.cardholder.ui.cards

import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import my.cardholder.data.model.Card
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardsViewModel @Inject constructor(
    cardRepository: CardRepository,
): BaseViewModel() {

    val cards: Flow<List<Card>> = cardRepository.cards

    fun onCardClicked(cardId: Long, extras: Navigator.Extras) {
        navigate(CardsFragmentDirections.fromCardsToCardViewer(cardId), extras)
    }
}
