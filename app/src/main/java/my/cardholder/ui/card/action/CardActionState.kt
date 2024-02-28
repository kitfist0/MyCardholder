package my.cardholder.ui.card.action

import my.cardholder.util.Text

data class CardActionState(
    val cardName: String,
    val isPinned: Boolean,
) {
    companion object {
        fun CardActionState.getTitleText(): Text {
            return Text.Simple("Choose action with card \"%s\"".format(cardName))
        }

        fun CardActionState.getPinUnpinButtonText(): Text {
            return Text.Simple(if (isPinned) "Unpin card" else "Pin card")
        }
    }
}
