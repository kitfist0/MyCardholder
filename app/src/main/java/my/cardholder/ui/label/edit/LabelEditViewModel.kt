package my.cardholder.ui.label.edit

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.R
import my.cardholder.data.source.LabelDao
import my.cardholder.data.model.Label
import my.cardholder.ui.base.BaseViewModel

class LabelEditViewModel @AssistedInject constructor(
    @Assisted("label_id") private val labelId: Long,
    private val labelDao: LabelDao,
) : BaseViewModel() {

    private companion object {
        const val NEW_LABEL_ID = 0L
    }

    private var enteredLabelText: String? = null

    private val _state = MutableStateFlow<LabelEditState>(LabelEditState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val labelText = labelDao.getLabel(labelId)?.text.orEmpty()
            _state.value = LabelEditState.Success(
                hintRes = if (labelId != NEW_LABEL_ID) {
                    R.string.label_edit_label_text_default_hint
                } else {
                    R.string.label_edit_label_text_new_hint
                },
                labelText = labelText,
            )
        }
    }

    fun onLabelTextChanged(labelText: String?) {
        enteredLabelText = labelText
        viewModelScope.launch {
            if (labelText.isNullOrBlank()) {
                showSnack("Invalid input")
            } else {
                labelDao.getLabelByText(labelText)
            }
        }
    }

    fun onOkFabClicked() {
        viewModelScope.launch {
            val labelText = enteredLabelText.orEmpty()
                .trim().replace("""\s+""".toRegex(), " ")
            when {
                labelText.isEmpty() ->
                    showSnack("Invalid input")
                labelId != NEW_LABEL_ID && labelDao.getLabelByText(labelText) != null ->
                    showSnack("Label already exists")
                else -> {
                    labelDao.upsert(Label(id = labelId, text = labelText))
                    navigateUp()
                }
            }
        }
    }
}

@AssistedFactory
interface LabelEditViewModelFactory {
    fun create(
        @Assisted("label_id") labelId: Long,
    ): LabelEditViewModel
}
