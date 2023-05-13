package my.cardholder.ui.label.action

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import my.cardholder.ui.base.BaseViewModel

class LabelActionViewModel @AssistedInject constructor(
    @Assisted("label_value") private val labelValue: String,
) : BaseViewModel()

@AssistedFactory
interface LabelActionViewModelFactory {
    fun create(
        @Assisted("label_value") labelValue: String,
    ): LabelActionViewModel
}
