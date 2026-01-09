package my.cardholder.ui.settings

import my.cardholder.R

enum class SettingId {
    THEME,
    COLUMNS,
    BRIGHTNESS,
    CATEGORIES,
    BACKUP,
    COFFEE,
    ABOUT
}

fun SettingId.getTitle(): Int = when (this) {
    SettingId.THEME -> R.string.settings_app_theme_item_title
    SettingId.COLUMNS -> R.string.settings_num_of_columns_item_title
    SettingId.BRIGHTNESS -> R.string.settings_brightness_item_title
    SettingId.CATEGORIES -> R.string.settings_card_categories_item_title
    SettingId.BACKUP -> R.string.settings_import_export_cards_item_title
    SettingId.COFFEE -> R.string.settings_coffee_item_title
    SettingId.ABOUT -> R.string.settings_about_app_item_title
}

fun SettingId.getImageRes(): Int = when (this) {
    SettingId.THEME -> R.drawable.ic_dark_mode
    SettingId.COLUMNS -> R.drawable.ic_list_single_column
    SettingId.BRIGHTNESS -> R.drawable.ic_brightness_max
    SettingId.CATEGORIES -> R.drawable.ic_category
    SettingId.BACKUP -> R.drawable.ic_import_export
    SettingId.COFFEE -> R.drawable.ic_coffee
    SettingId.ABOUT -> R.drawable.ic_info
}
