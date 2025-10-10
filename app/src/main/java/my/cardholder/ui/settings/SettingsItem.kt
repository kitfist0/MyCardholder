package my.cardholder.ui.settings

import my.cardholder.R

enum class SettingsItem {
    THEME,
    COLUMNS,
    CATEGORIES,
    BACKUP,
    COFFEE,
    ABOUT,
}

fun SettingsItem.getImageRes(): Int = when (this) {
    SettingsItem.THEME -> R.drawable.ic_dark_mode
    SettingsItem.COLUMNS -> R.drawable.ic_list_single_column
    SettingsItem.CATEGORIES -> R.drawable.ic_category
    SettingsItem.BACKUP -> R.drawable.ic_import_export
    SettingsItem.COFFEE -> R.drawable.ic_coffee
    SettingsItem.ABOUT -> R.drawable.ic_info
}

fun SettingsItem.getTitle(): String = when (this) {
    SettingsItem.THEME -> "App theme"
    SettingsItem.COLUMNS -> "Number of columns"
    SettingsItem.CATEGORIES -> "Card categories"
    SettingsItem.BACKUP -> "Import/export cards"
    SettingsItem.COFFEE -> "Coffee for developers"
    SettingsItem.ABOUT -> "About app"
}
