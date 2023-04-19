package my.cardholder.ui.card.list

import androidx.annotation.StringRes
import my.cardholder.R
import my.cardholder.data.model.Card

sealed class CardListState {
    data class Empty(@StringRes val messageRes: Int = R.string.cards_no_cards_message_text) : CardListState()
    data class Success(val cards: List<Card>, val spanCount: Int) : CardListState()
}
