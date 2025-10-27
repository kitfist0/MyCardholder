package my.cardholder.ui.settings

import androidx.annotation.DrawableRes

data class ListItem(
    val id: SettingId,
    @param:DrawableRes val iconRes: Int,
    val options: List<Option> = emptyList(),
) {
    data class Option(
        val id: String,
        val title: String,
        val selected: Boolean,
    )
}
