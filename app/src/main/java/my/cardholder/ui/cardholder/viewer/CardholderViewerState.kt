package my.cardholder.ui.cardholder.viewer

sealed class CardholderViewerState {
    object Loading : CardholderViewerState()

    data class Success(
        val cardName: String,
        val cardContent: String,
        val cardColor: Int,
        val barcodeFileName: String,
    ) : CardholderViewerState()
}
