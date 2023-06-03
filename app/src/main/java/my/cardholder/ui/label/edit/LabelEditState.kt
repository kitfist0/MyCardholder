package my.cardholder.ui.label.edit

import androidx.annotation.StringRes

sealed class LabelEditState {
    object Loading : LabelEditState()

    data class Success(
        @StringRes val hintRes: Int,
        val labelText: String,
    ) : LabelEditState()
}
