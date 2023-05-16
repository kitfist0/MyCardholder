package my.cardholder.ui.label.edit

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import my.cardholder.ui.base.BaseViewModel

class LabelEditViewModel @AssistedInject constructor(
    @Assisted("label_text") private val labelText: String,
) : BaseViewModel() {

    private val _state = MutableStateFlow<LabelEditState>(LabelEditState.Success(labelText))
    val state = _state.asStateFlow()

    fun onOkFabClicked() {
        navigateUp()
    }
}

@AssistedFactory
interface LabelEditViewModelFactory {
    fun create(
        @Assisted("label_text") labelText: String,
    ): LabelEditViewModel
}
