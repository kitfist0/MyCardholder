package my.cardholder.ui.label.list

import androidx.annotation.StringRes
import my.cardholder.R
import my.cardholder.data.model.Label

sealed class LabelListState {
    data class Empty(
        @StringRes val messageRes: Int = R.string.label_list_empty_list_message
    ) : LabelListState()

    data class Success(val labels: List<Label>) : LabelListState()
}
