package my.cardholder.ui.card.content

sealed class CardContentState {
    object Loading : CardContentState()

    data class Success(val cardContent: String) : CardContentState()
}
