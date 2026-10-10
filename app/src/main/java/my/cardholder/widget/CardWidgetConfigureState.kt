package my.cardholder.widget

import my.cardholder.data.model.Card

data class CardWidgetConfigureState(
    val items: List<Item> = emptyList(),
    val isSaveEnabled: Boolean = false,
    val isLoading: Boolean = true,
) {
    data class Item(
        val card: Card,
        val isSelected: Boolean,
    )
}
