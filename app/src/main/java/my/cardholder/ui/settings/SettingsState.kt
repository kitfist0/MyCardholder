package my.cardholder.ui.settings

sealed class SettingsListItem {
    data class Header(val cloudSyncEnabled: Boolean, val cloudName: String? = null) : SettingsListItem()
    data class Item(val id: SettingId, val subtitle: String? = null) : SettingsListItem()
}

data class SettingsState(
    val settingsItems: List<SettingsListItem>,
)
