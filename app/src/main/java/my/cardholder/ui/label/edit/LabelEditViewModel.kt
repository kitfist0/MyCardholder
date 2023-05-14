package my.cardholder.ui.label.edit

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import my.cardholder.ui.base.BaseViewModel

class LabelEditViewModel @AssistedInject constructor(
    @Assisted("label_value") private val labelValue: String,
) : BaseViewModel() {

    private val _state = MutableStateFlow<LabelEditState>(LabelEditState.Success(labelValue))
    val state = _state.asStateFlow()

    fun onOkFabClicked() {
        navigateUp()
    }
}

@AssistedFactory
interface LabelEditViewModelFactory {
    fun create(
        @Assisted("label_value") labelValue: String,
    ): LabelEditViewModel
}
