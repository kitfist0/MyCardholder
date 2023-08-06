package my.cardholder.ui.card.delete

import my.cardholder.R
import my.cardholder.util.Text

data class DeleteCardState(
    val cardName: String,
) {
    companion object {
        fun DeleteCardState.getTitleText(): Text {
            return Text.ResourceAndParams(R.string.delete_card_dialog_title, listOf(cardName))
        }
    }
}
