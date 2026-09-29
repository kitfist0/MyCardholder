package my.cardholder.shortcut

import android.content.Intent
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
class CardShortcutCreateViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val cardShortcutManager: CardShortcutManager,
) : ViewModel() {

    val cards = cardRepository.cardsAndCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val shortcutCreatedChannel = Channel<Intent>()
    val shortcutCreatedEvent = shortcutCreatedChannel.receiveAsFlow()

    private var isCreatingShortcut = false

    fun onCardSelected(cardId: Long) {
        if (isCreatingShortcut) return
        isCreatingShortcut = true
        viewModelScope.launch {
            val card = cardRepository.getCard(cardId)
            if (card == null) {
                isCreatingShortcut = false
                return@launch
            }
            shortcutCreatedChannel.send(cardShortcutManager.createShortcutResultIntent(card))
        }
    }
}
