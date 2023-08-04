package my.cardholder.ui.card.search

import my.cardholder.data.model.Card

sealed class CardSearchState {
    data class Default(
        val categoryNames: List<String>,
        val selectedCategoryName: String?,
    ) : CardSearchState() {
        companion object {
            fun Default.getHint(): String {
                return selectedCategoryName
                    ?.let { "Search within \"$it\"" }
                    ?: "Search in all categories"
            }
        }
    }

    data class Success(val cards: List<Card>) : CardSearchState()

    object NothingFound : CardSearchState()
}
