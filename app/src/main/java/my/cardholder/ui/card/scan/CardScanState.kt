package my.cardholder.ui.card.scan

import my.cardholder.data.model.ScanResult

data class CardScanState(
    val withExplanation: Boolean,
    val preliminaryScanResult: ScanResult.Success?,
    val launchBarcodeFileSelectionRequest: Boolean,
)
