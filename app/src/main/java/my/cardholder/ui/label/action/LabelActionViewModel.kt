package my.cardholder.ui.label.action

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.cardholder.data.source.LabelDao
import my.cardholder.ui.base.BaseViewModel

class LabelActionViewModel @AssistedInject constructor(
    @Assisted("label_id") private val labelId: Long,
    private val labelDao: LabelDao,
) : BaseViewModel() {

    private val _state = MutableStateFlow(LabelActionState(""))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val labelText = labelDao.getLabel(labelId)?.text.orEmpty()
            _state.value = LabelActionState(labelText)
        }
    }

    fun onDeleteButtonClicked() {
        viewModelScope.launch {
            labelDao.deleteLabel(labelId)
            navigateUp()
        }
    }

    fun onEditButtonClicked() {
        navigate(LabelActionDialogDirections.fromLabelActionToLabelEdit(labelId))
    }
}

@AssistedFactory
interface LabelActionViewModelFactory {
    fun create(
        @Assisted("label_id") labelId: Long,
    ): LabelActionViewModel
}
