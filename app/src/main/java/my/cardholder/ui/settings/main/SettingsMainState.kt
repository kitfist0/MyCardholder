package my.cardholder.ui.settings.main

data class SettingsMainState(
    val nightModeEnabled: Boolean,
    val multiColumnListEnabled: Boolean,
    val launchCardsExport: Boolean,
    val launchCardsImport: Boolean,
)
