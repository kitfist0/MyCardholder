package my.cardholder.ui.settings

data class HeaderState(
    val cloudSyncEnabled: Boolean,
    val cloudName: String? = null,
)

data class SettingsState(
    val headerState: HeaderState,
    val settingsItems: List<ListItem>,
)
