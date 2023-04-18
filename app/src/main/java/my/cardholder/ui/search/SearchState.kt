package my.cardholder.ui.search

import androidx.annotation.StringRes
import my.cardholder.data.model.Card

sealed class SearchState {
    data class Empty(@StringRes val messageRes: Int) : SearchState()
    data class Success(val cards: List<Card>) : SearchState()
}
