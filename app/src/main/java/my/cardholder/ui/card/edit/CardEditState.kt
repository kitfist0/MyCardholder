package my.cardholder.ui.card.edit

import my.cardholder.data.model.Card
import my.cardholder.data.model.SupportedFormat
import java.io.File

sealed class CardEditState {
    object Loading : CardEditState()

    data class Success(
        val cardName: String,
        val cardContent: String,
        val cardColor: Int,
        val barcodeFile: File?,
        val barcodeFormatName: String,
        val barcodeFormatNames: List<String> = SupportedFormat.values().map { it.toString() },
        val cardColors: List<String> = Card.COLORS.toList(),
    ) : CardEditState()
}
