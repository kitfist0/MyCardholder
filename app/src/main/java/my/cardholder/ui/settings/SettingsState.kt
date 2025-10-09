package my.cardholder.ui.settings

import my.cardholder.util.Text

sealed class SettingsListItem {
    data class Header(val cloudSyncEnabled: Boolean, val cloudName: String? = null) : SettingsListItem()
    data class Item(val id: SettingsItem, val subtitle: String? = null) : SettingsListItem()
}

data class SettingsState(
    val nightModeEnabled: Boolean,
    val multiColumnListEnabled: Boolean,
    val cloudSyncCardText: Text,
    val cloudSyncEnabled: Boolean,
)
