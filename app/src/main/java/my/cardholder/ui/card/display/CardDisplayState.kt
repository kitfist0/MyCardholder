package my.cardholder.ui.card.display

import java.io.File

sealed class CardDisplayState {
    data object Loading : CardDisplayState()

    data class Success(
        val barcodeFile: File?,
        val cardCategory: String,
        val cardLogo: String?,
        val cardName: String,
        val cardContent: String,
        val cardComment: String,
        val cardColor: Int,
    ) : CardDisplayState()
}
