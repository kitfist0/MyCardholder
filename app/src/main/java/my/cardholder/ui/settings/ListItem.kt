package my.cardholder.ui.settings

data class ListItem(
    val id: String,
    val title: String,
    val options: List<Option> = emptyList(),
) {
    data class Option(
        val id: String,
        val title: String,
        val selected: Boolean,
    )
}
