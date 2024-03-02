package my.cardholder.ui.card.list

import androidx.annotation.StringRes
import my.cardholder.R
import my.cardholder.data.model.CardAndCategory

sealed class CardListState {
    data class Empty(
        @StringRes val messageRes: Int = R.string.card_list_no_cards_message
    ) : CardListState()

    data class Success(
        val cardsAndCategories: List<CardAndCategory>,
        val spanCount: Int,
        val scrollUpEvent: Boolean,
    ) : CardListState()
}
