package my.cardholder.ui.card.search

sealed class CardSearchCategoryItem {
    sealed class HeaderItem : CardSearchCategoryItem() {
        data object AddCategories : HeaderItem()
        data object EditCategories : HeaderItem()
    }
    data class DefaultItem(val name: String) : CardSearchCategoryItem()
}
