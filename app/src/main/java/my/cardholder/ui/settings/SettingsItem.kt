package my.cardholder.ui.settings

enum class SettingsItem {
    THEME,
    COLUMNS,
    CATEGORIES,
    BACKUP,
    COFFEE,
    ABOUT,
}

fun SettingsItem.getTitle(): String = when (this) {
    SettingsItem.THEME -> "Theme"
    SettingsItem.COLUMNS -> "List appearance"
    SettingsItem.CATEGORIES -> "Card categories"
    SettingsItem.BACKUP -> "Import/export cards"
    SettingsItem.COFFEE -> "Coffee for developers"
    SettingsItem.ABOUT -> "About app"
}
