package my.cardholder.ui.card.search

import my.cardholder.R
import my.cardholder.data.model.Card
import my.cardholder.util.Text

sealed class CardSearchState {
    data class Default(
        val categoryNames: List<String>,
        val selectedCategoryName: String?,
    ) : CardSearchState() {
        companion object {
            fun Default.getHint(): Text {
                return selectedCategoryName
                    ?.let { Text.ResourceAndParams(R.string.card_search_within_category_hint, listOf(it)) }
                    ?: Text.Resource(R.string.card_search_in_all_categories_hint)
            }
        }
    }

    data class Success(val cards: List<Card>) : CardSearchState()

    data object NothingFound : CardSearchState()
}
