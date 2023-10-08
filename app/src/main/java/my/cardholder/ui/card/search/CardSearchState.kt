package my.cardholder.ui.card.search

import my.cardholder.data.model.Card

sealed class CardSearchState {
    data class SearchInAllCategories(
        val categories: List<CardSearchCategoryItem>,
    ) : CardSearchState()

    data class SearchInCategory(
        val categoryName: String,
        val categoryCards: List<Card>,
    ) : CardSearchState()

    data class Success(val cards: List<Card>) : CardSearchState()

    data object NothingFound : CardSearchState()
}
