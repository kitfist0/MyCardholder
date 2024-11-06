package my.cardholder.ui.card.zoom

import java.io.File

sealed class CardZoomState {
    data object Loading : CardZoomState()

    data class Success(
        val barcodeFile: File?,
        val cardColor: Int,
    ) : CardZoomState()
}
