package my.cardholder.ui.scanner.permission

sealed class ScannerPermissionState {
    object Loading : ScannerPermissionState()

    data class PermissionRequired(
        val requiredPermission: String,
        val rationaleTextStringRes: Int,
        val requestPermission: Boolean,
    ) : ScannerPermissionState()
}
