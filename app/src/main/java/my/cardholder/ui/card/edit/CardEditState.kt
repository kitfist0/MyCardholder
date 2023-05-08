package my.cardholder.ui.card.edit

import my.cardholder.data.model.Card
import my.cardholder.data.model.Label
import my.cardholder.data.model.SupportedFormat
import java.io.File

sealed class CardEditState {
    object Loading : CardEditState()

    data class Success(
        val barcodeFile: File?,
        val cardLabels: List<String>,
        val cardName: String,
        val cardContent: String,
        val barcodeFormatName: String,
        val barcodeFormatNames: List<String> = SupportedFormat.values().map { it.toString() },
        val cardColor: String,
        val cardColors: List<String> = Card.COLORS.toList(),
    ) : CardEditState()
}
