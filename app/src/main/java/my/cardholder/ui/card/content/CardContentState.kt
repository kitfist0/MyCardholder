package my.cardholder.ui.card.content

sealed class CardContentState {
    data object Loading : CardContentState()

    data class Success(val cardContent: String) : CardContentState()
}
