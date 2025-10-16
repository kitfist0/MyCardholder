package my.cardholder.ui.settings

import my.cardholder.R

enum class SettingId {
    THEME,
    COLUMNS,
    CATEGORIES,
    BACKUP,
    COFFEE,
    ABOUT;
}

data class ListItem(
    val id: SettingId,
    val options: List<Option> = emptyList(),
) {
    data class Option(
        val id: String,
        val title: String,
        val selected: Boolean,
    )
}

fun SettingId.getTitle(): String = when (this) {
    SettingId.THEME -> "App theme"
    SettingId.COLUMNS -> "Number of columns"
    SettingId.CATEGORIES -> "Card categories"
    SettingId.BACKUP -> "Import/export cards"
    SettingId.COFFEE -> "Coffee for developers"
    SettingId.ABOUT -> "About app"
}

fun SettingId.getImageRes(): Int = when (this) {
    SettingId.THEME -> R.drawable.ic_dark_mode
    SettingId.COLUMNS -> R.drawable.ic_list_single_column
    SettingId.CATEGORIES -> R.drawable.ic_category
    SettingId.BACKUP -> R.drawable.ic_import_export
    SettingId.COFFEE -> R.drawable.ic_coffee
    SettingId.ABOUT -> R.drawable.ic_info
}
