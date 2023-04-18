package my.cardholder.ui.viewer

import java.io.File

sealed class ViewerState {
    object Loading : ViewerState()

    data class Success(
        val cardName: String,
        val cardContent: String,
        val cardColor: Int,
        val barcodeFile: File?,
    ) : ViewerState()
}
