package my.cardholder.ui.card.scan

import my.cardholder.data.model.ScanResult

data class CardScanState(
    val explanationIsVisible: Boolean,
    val preliminaryScanResult: ScanResult.Success?,
    val launchBarcodeFileSelectionRequest: Boolean,
)
