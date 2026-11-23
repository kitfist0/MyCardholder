package my.cardholder.ui.settings

import my.cardholder.R

enum class SettingId {
    THEME,
    DYNAMIC_COLORS,
    COLUMNS,
    CATEGORIES,
    BACKUP,
    SUPPORT,
    COFFEE,
    ABOUT
}

fun SettingId.getTitle(): Int = when (this) {
    SettingId.THEME -> R.string.settings_app_theme_item_title
    SettingId.DYNAMIC_COLORS -> R.string.settings_dynamic_colors_item_title
    SettingId.COLUMNS -> R.string.settings_num_of_columns_item_title
    SettingId.CATEGORIES -> R.string.settings_card_categories_item_title
    SettingId.BACKUP -> R.string.settings_import_export_cards_item_title
    SettingId.SUPPORT -> R.string.settings_support_item_title
    SettingId.COFFEE -> R.string.settings_coffee_item_title
    SettingId.ABOUT -> R.string.settings_about_app_item_title
}

fun SettingId.getImageRes(): Int = when (this) {
    SettingId.THEME -> R.drawable.ic_dark_mode
    SettingId.DYNAMIC_COLORS -> R.drawable.ic_palette_off
    SettingId.COLUMNS -> R.drawable.ic_list_single_column
    SettingId.CATEGORIES -> R.drawable.ic_category
    SettingId.BACKUP -> R.drawable.ic_import_export
    SettingId.SUPPORT -> R.drawable.ic_favorite
    SettingId.COFFEE -> R.drawable.ic_coffee
    SettingId.ABOUT -> R.drawable.ic_info
}
