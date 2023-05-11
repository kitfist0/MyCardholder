package my.cardholder.ui.labels

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import my.cardholder.data.LabelDao
import my.cardholder.data.model.Label
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class LabelsViewModel @Inject constructor(
    labelDao: LabelDao,
) : BaseViewModel() {

    private val _state = MutableStateFlow<LabelsState>(LabelsState.Empty())
    val state = _state.asStateFlow()

    init {
        labelDao.getLabels()
            .onEach { labels ->
                _state.value = if (labels.isNotEmpty()) {
                    LabelsState.Success(labels)
                } else {
                    LabelsState.Empty()
                }
            }
            .launchIn(viewModelScope)
    }

    fun onLabelClicked(label: Label) {
    }

    fun onAddLabelFabClicked() {
    }
}
