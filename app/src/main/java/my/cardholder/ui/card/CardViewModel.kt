package my.cardholder.ui.card

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import my.cardholder.data.Card
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _card = MutableLiveData<Card>().apply {
        value = Card(
            id = CardFragmentArgs.fromSavedStateHandle(savedStateHandle).cardId,
            title = "Card title",
            text = "b6589fc6ab0dc82cf12099d1c2d40ab994e8410c",
            color = "",
            format = "qr_code",
            time = 0L,
        )
    }
    val card: LiveData<Card> = _card
}
