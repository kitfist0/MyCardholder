package my.cardholder.ui.card.search

sealed class CardSearchCategoryItem {
    data object HeaderItem : CardSearchCategoryItem()
    data class DefaultItem(val name: String) : CardSearchCategoryItem()
}
