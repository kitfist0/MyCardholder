package my.cardholder.ui.card.search

import my.cardholder.data.model.Card

sealed class CardSearchState {
    data class Default(val categoryNames: List<String>) : CardSearchState()
    data class Success(val cards: List<Card>) : CardSearchState()
    object NothingFound : CardSearchState()
}
