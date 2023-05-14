package my.cardholder.ui.label.edit

sealed class LabelEditState {
    object Loading : LabelEditState()

    data class Success(
        val labelText: String,
    ) : LabelEditState()
}
