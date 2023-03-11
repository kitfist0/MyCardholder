package my.cardholder.ui.cardholder.viewer

import java.io.File

sealed class CardholderViewerState {
    object Loading : CardholderViewerState()

    data class Success(
        val cardName: String,
        val cardContent: String,
        val cardColor: Int,
        val barcodeFile: File?,
    ) : CardholderViewerState()
}
