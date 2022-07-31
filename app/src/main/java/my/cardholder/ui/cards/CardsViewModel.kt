package my.cardholder.ui.cards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import my.cardholder.data.Card
import javax.inject.Inject

@HiltViewModel
class CardsViewModel @Inject constructor(): ViewModel() {

    companion object {
        private const val TAG = "CARDS_VIEW_MODEL"
    }

    private val _navigateTo = MutableLiveData<NavDirections>()
    val navigateTo: LiveData<NavDirections> = _navigateTo

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
    val cards: LiveData<List<Card>> = _cards

    fun onCardClicked(cardId: Long) {
        _navigateTo.value = CardsFragmentDirections.fromCardsToCard(cardId)
    }
}
