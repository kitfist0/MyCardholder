package my.cardholder.ui.cards

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.navigation.NavDirections
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import my.cardholder.data.Card
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardsViewModel @Inject constructor(): BaseViewModel() {

    private val eventChannel = Channel<Pair<NavDirections, Navigator.Extras>>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    private val _cards = MutableLiveData<List<Card>>().apply {
        value = listOf(
            Card(
                id = 0,
                title = "Card0",
                text = "b6589fc6ab0dc82cf12099d1c2d40ab994e8410c",
                color = "",
                format = "qr_code",
                time = 0L,
            ),
            Card(
                id = 1,
                title = "Card1",
                text = "356a192b7913b04c54574d18c28d46e6395428ab",
                color = "",
                format = "qr_code",
                time = 0L,
            ),
        )
    }
    val cards: Flow<List<Card>> = _cards.asFlow()

    fun onCardClicked(cardId: Long, extras: Navigator.Extras) {
        eventChannel.trySend(CardsFragmentDirections.fromCardsToCard(cardId) to extras)
    }
}
