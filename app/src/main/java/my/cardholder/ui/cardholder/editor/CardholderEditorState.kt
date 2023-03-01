package my.cardholder.ui.cardholder.editor

import my.cardholder.data.model.Card
import my.cardholder.data.model.SupportedFormat

sealed class CardholderEditorState {
    object Loading : CardholderEditorState()

    data class Success(
        val cardName: String,
        val cardContent: String,
        val cardColor: Int,
        val barcodeFileName: String,
        val barcodeFormatName: String,
        val barcodeFormatNames: List<String> = SupportedFormat.values().map { it.toString() },
        val cardColors: List<String> = Card.COLORS.toList(),
    ) : CardholderEditorState()
}
