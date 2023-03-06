package my.cardholder.ui.cardholder.cards

import androidx.annotation.StringRes
import my.cardholder.R
import my.cardholder.data.model.Card

sealed class CardholderCardsState {
    data class Empty(@StringRes val messageRes: Int = R.string.cards_no_cards_message_text) : CardholderCardsState()
    data class Success(val cards: List<Card>, val spanCount: Int) : CardholderCardsState()
}
