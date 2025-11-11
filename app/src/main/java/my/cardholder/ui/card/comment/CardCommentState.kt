package my.cardholder.ui.card.comment

sealed class CardCommentState {
    data object Loading : CardCommentState()
    data class Success(val comment: String) : CardCommentState()
}
