package my.cardholder.ui.cardholder.editor

import my.cardholder.data.model.Card

sealed class CardholderEditorState {
    object Loading : CardholderEditorState()

    data class Success(
        val cardName: String,
        val cardText: String,
        val cardColor: Int,
        val barcodeFileName: String,
        val cardColors: List<String> = Card.COLORS.toList(),
    ) : CardholderEditorState()
}
