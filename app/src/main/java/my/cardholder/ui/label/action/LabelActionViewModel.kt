package my.cardholder.ui.label.action

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.cardholder.data.LabelDao
import my.cardholder.data.model.Label
import my.cardholder.ui.base.BaseViewModel

class LabelActionViewModel @AssistedInject constructor(
    @Assisted("label_text") private val labelText: String,
    private val labelDao: LabelDao,
) : BaseViewModel() {

    private val _state = MutableStateFlow(LabelActionState(labelText))
    val state = _state.asStateFlow()

    fun onDeleteButtonClicked() {
        viewModelScope.launch {
            labelDao.delete(Label(labelText))
            navigateUp()
        }
    }

    fun onEditButtonClicked() {
        navigate(LabelActionDialogDirections.fromLabelActionToLabelEdit(labelText))
    }
}

@AssistedFactory
interface LabelActionViewModelFactory {
    fun create(
        @Assisted("label_text") labelText: String,
    ): LabelActionViewModel
}
