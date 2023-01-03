package my.cardholder.ui.cardholder.editor

import my.cardholder.data.model.Card

sealed class CardholderEditorState {
    object Loading : CardholderEditorState()
    data class Success(
        val card: Card,
        val cardColors: List<String> = Card.COLORS.toList(),
    ) : CardholderEditorState()
}
