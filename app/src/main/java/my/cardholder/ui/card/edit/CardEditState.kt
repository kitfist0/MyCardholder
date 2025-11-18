package my.cardholder.ui.card.edit

import my.cardholder.data.model.Card
import my.cardholder.data.model.SupportedFormat
import java.io.File

sealed class CardEditState {
    data object Loading : CardEditState()

    data class Success(
        val barcodeFile: File?,
        val cardName: String,
        val cardContent: String,
        val cardCategoryName: String,
        val cardCategoryNames: List<String>,
        val cardColor: String,
        val cardColors: List<String> = Card.COLORS.toList(),
        val cardComment: String,
        val cardLogo: String,
        val barcodeFormatName: String,
        val barcodeFormatNames: List<String> = SupportedFormat.entries.map { it.toString() },
    ) : CardEditState()
}
