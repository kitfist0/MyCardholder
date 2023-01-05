package my.cardholder.ui.cardholder.viewer

sealed class CardholderViewerState {
    object Loading : CardholderViewerState()

    data class Success(
        val cardName: String,
        val cardText: String,
        val cardColor: Int,
        val barcodeFileName: String,
    ) : CardholderViewerState()
}
