package my.cardholder.ui.permission

sealed class PermissionState {
    object Loading : PermissionState()

    data class PermissionRequired(
        val requiredPermission: String,
        val rationaleTextStringRes: Int,
        val requestPermission: Boolean,
    ) : PermissionState()
}
