package my.cardholder.ui.card.labels

import androidx.annotation.StringRes
import my.cardholder.R

sealed class CardLabelsState {
    data class Empty(
        val cardName: String,
        @StringRes val messageRes: Int = R.string.label_list_empty_list_message,
    ) : CardLabelsState()

    data class Success(
        val cardName: String,
        val cardLabels: List<CardLabelsItemState>,
    ) : CardLabelsState()
}
