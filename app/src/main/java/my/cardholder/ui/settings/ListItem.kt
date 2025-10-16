package my.cardholder.ui.settings

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
