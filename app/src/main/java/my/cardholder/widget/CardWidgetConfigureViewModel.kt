package my.cardholder.widget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import javax.inject.Inject

@HiltViewModel
class CardWidgetConfigureViewModel @Inject constructor(
    cardRepository: CardRepository,
    private val cardWidgetIdStore: CardWidgetIdStore,
) : ViewModel() {

    val cards = cardRepository.cardsAndCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val widgetConfiguredChannel = Channel<Unit>()
    val widgetConfiguredEvent = widgetConfiguredChannel.receiveAsFlow()

    fun onCardSelected(appWidgetId: Int, cardId: Long) {
        cardWidgetIdStore.saveCardId(appWidgetId, cardId)
        viewModelScope.launch {
            widgetConfiguredChannel.send(Unit)
        }
    }
}
