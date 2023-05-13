package my.cardholder.ui.label.list

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import my.cardholder.data.LabelDao
import my.cardholder.data.model.Label
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class LabelListViewModel @Inject constructor(
    labelDao: LabelDao,
) : BaseViewModel() {

    private val _state = MutableStateFlow<LabelListState>(LabelListState.Empty())
    val state = _state.asStateFlow()

    init {
        labelDao.getLabels()
            .onEach { labels ->
                _state.value = if (labels.isNotEmpty()) {
                    LabelListState.Success(labels)
                } else {
                    LabelListState.Empty()
                }
            }
            .launchIn(viewModelScope)
    }

    fun onLabelClicked(label: Label) {
        navigate(LabelListFragmentDirections.fromLabelListToLabelAction(label.value))
    }

    fun onAddLabelFabClicked() {
    }
}
