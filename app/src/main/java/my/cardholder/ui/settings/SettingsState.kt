package my.cardholder.ui.settings

import android.content.Intent

data class SettingsState(
    val nightModeEnabled: Boolean,
    val multiColumnListEnabled: Boolean,
    val cloudSyncAvailable: Boolean,
    val cloudSyncEnabled: Boolean,
    val launchCloudSignInRequest: Intent? = null,
)
