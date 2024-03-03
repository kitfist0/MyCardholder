package my.cardholder.ui.card.action

import my.cardholder.R
import my.cardholder.util.Text

data class CardActionState(
    val cardName: String,
    val isPinned: Boolean,
) {
    companion object {
        fun CardActionState.getTitleText(): Text {
            return Text.ResourceAndParams(
                R.string.card_action_dialog_default_title,
                listOf(cardName)
            )
        }

        fun CardActionState.getPinUnpinButtonText(): Text {
            return Text.Resource(
                if (isPinned) {
                    R.string.card_action_dialog_unpin_card_button_text
                } else {
                    R.string.card_action_dialog_pin_card_button_text
                }
            )
        }
    }
}
