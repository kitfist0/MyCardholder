package my.cardholder.ui.card.search

import androidx.annotation.StringRes
import my.cardholder.data.model.Card

sealed class CardSearchState {
    data class Empty(@StringRes val messageRes: Int) : CardSearchState()
    data class Success(val cards: List<Card>) : CardSearchState()
}
