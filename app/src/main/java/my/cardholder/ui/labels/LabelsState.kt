package my.cardholder.ui.labels

import androidx.annotation.StringRes
import my.cardholder.R
import my.cardholder.data.model.Label

sealed class LabelsState {
    data class Empty(
        @StringRes val messageRes: Int = R.string.labels_empty_list_message
    ) : LabelsState()

    data class Success(val labels: List<Label>) : LabelsState()
}
