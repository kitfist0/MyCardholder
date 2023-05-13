package my.cardholder.ui.card.labels

import androidx.annotation.StringRes
import my.cardholder.R
import my.cardholder.data.model.Label

sealed class CardLabelsState {
    data class Empty(
        @StringRes val messageRes: Int = R.string.label_list_empty_list_message
    ) : CardLabelsState()

    data class Success(val labels: List<Label>) : CardLabelsState()
}
