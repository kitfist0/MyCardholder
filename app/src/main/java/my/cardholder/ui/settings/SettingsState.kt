package my.cardholder.ui.settings

data class SettingsState(
    val nightModeEnabled: Boolean,
    val multiColumnListEnabled: Boolean,
    val cloudSyncEnabled: Boolean,
    val launchCloudSignInRequest: Boolean,
)
