package my.cardholder.ui.card.display

import java.io.File

sealed class CardDisplayState {
    object Loading : CardDisplayState()

    data class Success(
        val cardName: String,
        val cardContent: String,
        val cardColor: Int,
        val barcodeFile: File?,
    ) : CardDisplayState()
}
