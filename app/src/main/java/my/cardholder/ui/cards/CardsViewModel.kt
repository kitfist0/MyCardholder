package my.cardholder.ui.cards

import androidx.lifecycle.asFlow
import androidx.lifecycle.liveData
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import my.cardholder.data.Card
import my.cardholder.data.CardDao
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardsViewModel @Inject constructor(
    cardDao: CardDao,
): BaseViewModel() {

    private val _cards = liveData {
        emit(cardDao.getCards())
    }
    val cards: Flow<List<Card>> = _cards.asFlow()

    fun onCardClicked(cardId: Long, extras: Navigator.Extras) {
        navigate(CardsFragmentDirections.fromCardsToCardViewer(cardId), extras)
    }
}
