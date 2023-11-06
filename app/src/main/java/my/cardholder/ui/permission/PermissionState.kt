package my.cardholder.ui.permission

import my.cardholder.R

sealed class PermissionState {
    data object Loading : PermissionState()

    data class PermissionRequired(
        val rationaleTextStringRes: Int = R.string.permission_rationale_message_text,
        val launchBarcodeFileSelectionRequest: Boolean,
        val launchCameraPermissionRequest: Boolean,
    ) : PermissionState()
}
