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
    @Assisted("label_value") private val labelValue: String,
    private val labelDao: LabelDao,
) : BaseViewModel() {

    private val _state = MutableStateFlow(LabelActionState(labelValue))
    val state = _state.asStateFlow()

    fun onDeleteButtonClicked() {
        viewModelScope.launch {
            labelDao.delete(Label(labelValue))
            navigateUp()
        }
    }

    fun onEditButtonClicked() {
        navigate(LabelActionDialogDirections.fromLabelActionToLabelEdit(labelValue))
    }
}

@AssistedFactory
interface LabelActionViewModelFactory {
    fun create(
        @Assisted("label_value") labelValue: String,
    ): LabelActionViewModel
}
