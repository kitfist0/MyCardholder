package my.cardholder.ui.card.edit

import my.cardholder.data.model.Card
import my.cardholder.data.model.LogoSuggestion
import my.cardholder.data.model.SupportedFormat
import java.io.File

sealed class CardEditState {
    data object Loading : CardEditState()

    data class Success(
        val barcodeFile: File?,
        val cardName: String,
        val cardNameIsEmpty: Boolean = false,
        val cardContent: String,
        /** Null when the card has no category */
        val cardCategoryName: String?,
        val cardCategoryNames: List<String>,
        val cardComment: String,
        val cardColor: String,
        val cardColors: List<String> = Card.COLORS.toList(),
        val cardLogo: String,
        /** Offered while the card has no logo */
        val logoSuggestions: List<LogoSuggestion> = emptyList(),
        val barcodeFormatName: String,
        val barcodeFormatNames: List<String> = SupportedFormat.entries.map { it.toString() },
    ) : CardEditState()
}
