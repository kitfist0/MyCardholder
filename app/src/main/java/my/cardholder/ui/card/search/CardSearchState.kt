package my.cardholder.ui.card.search

import androidx.annotation.StringRes
import my.cardholder.R
import my.cardholder.data.model.Card

sealed class CardSearchState {
    data class Default(
        val message: String = Typography.nbsp.toString()
    ) : CardSearchState()

    data class NothingFound(
        @StringRes val messageRes: Int = R.string.card_search_nothing_found_message
    ) : CardSearchState()

    data class Success(val cards: List<Card>) : CardSearchState()
}
