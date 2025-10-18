package my.cardholder.ui.settings

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
