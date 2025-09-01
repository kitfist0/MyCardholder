package my.cardholder.ui.settings

import android.content.Intent
import my.cardholder.util.Text

data class SettingsState(
    val nightModeEnabled: Boolean,
    val multiColumnListEnabled: Boolean,
    val cloudSyncCardText: Text,
    val cloudSyncEnabled: Boolean,
    val launchCloudSignInRequest: Intent? = null,
)
