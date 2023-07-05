package my.cardholder.ui.card.display

import java.io.File

sealed class CardDisplayState {
    object Loading : CardDisplayState()

    data class Success(
        val barcodeFile: File?,
        val cardCategory: String,
        val cardName: String,
        val cardContent: String,
        val cardColor: Int,
    ) : CardDisplayState()
}
