package my.cardholder.ui.cards

import androidx.annotation.StringRes
import my.cardholder.R
import my.cardholder.data.model.Card

sealed class CardsState {
    data class Empty(@StringRes val messageRes: Int = R.string.cards_no_cards_message_text) : CardsState()
    data class Success(val cards: List<Card>, val spanCount: Int) : CardsState()
}
