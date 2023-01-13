package my.cardholder.ui.cardholder.search

import androidx.annotation.StringRes
import my.cardholder.data.model.Card

sealed class CardholderSearchState {
    data class Empty(@StringRes val messageRes: Int) : CardholderSearchState()
    data class Success(val cards: List<Card>) : CardholderSearchState()
}
