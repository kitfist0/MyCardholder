package my.cardholder.data.model

sealed class ScanResult {
    data class Failure(val throwable: Throwable) : ScanResult()
    data class Success(val content: String, val format: SupportedFormat) : ScanResult()
    data object Nothing : ScanResult()
}
