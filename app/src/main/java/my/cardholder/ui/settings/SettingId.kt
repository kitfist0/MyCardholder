package my.cardholder.ui.settings

import my.cardholder.R

enum class SettingId {
    THEME,
    COLUMNS,
    CATEGORIES,
    BACKUP,
    COFFEE,
    ABOUT
}

fun SettingId.getTitle(): Int = when (this) {
    SettingId.THEME -> R.string.settings_app_theme_title
    SettingId.COLUMNS -> R.string.settings_num_of_columns_title
    SettingId.CATEGORIES -> R.string.settings_card_categories_button_text
    SettingId.BACKUP -> R.string.settings_import_export_cards_button_text
    SettingId.COFFEE -> R.string.coffee_dialog_title
    SettingId.ABOUT -> R.string.settings_about_app_button_text
}

fun SettingId.getImageRes(): Int = when (this) {
    SettingId.THEME -> R.drawable.ic_dark_mode
    SettingId.COLUMNS -> R.drawable.ic_list_single_column
    SettingId.CATEGORIES -> R.drawable.ic_category
    SettingId.BACKUP -> R.drawable.ic_import_export
    SettingId.COFFEE -> R.drawable.ic_coffee
    SettingId.ABOUT -> R.drawable.ic_info
}
