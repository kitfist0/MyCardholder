package my.cardholder.ui.settings

data class SettingsState(
    val headerState: HeaderState,
    val settingsItems: List<SettingsItem>,
) {
    data class HeaderState(
        val cloudSyncEnabled: Boolean,
        val cloudName: String? = null,
    )
}
