package my.cardholder.ui.settings

data class SettingsState(
    val nightModeEnabled: Boolean,
    val multiColumnListEnabled: Boolean,
    val launchCardsExport: Boolean,
    val launchCardsImport: Boolean,
)
