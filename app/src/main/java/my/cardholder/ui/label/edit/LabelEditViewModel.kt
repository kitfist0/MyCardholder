package my.cardholder.ui.label.edit

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.LabelDao
import my.cardholder.ui.base.BaseViewModel

class LabelEditViewModel @AssistedInject constructor(
    @Assisted("label_id") private val labelId: Long,
    private val labelDao: LabelDao,
) : BaseViewModel() {

    private val _state = MutableStateFlow<LabelEditState>(LabelEditState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val labelText = labelDao.getLabel(labelId)?.text.orEmpty()
            _state.value = LabelEditState.Success(labelText)
        }
    }

    fun onLabelTextChanged(labelText: String?) {
    }

    fun onOkFabClicked() {
        navigateUp()
    }
}

@AssistedFactory
interface LabelEditViewModelFactory {
    fun create(
        @Assisted("label_id") labelId: Long,
    ): LabelEditViewModel
}
