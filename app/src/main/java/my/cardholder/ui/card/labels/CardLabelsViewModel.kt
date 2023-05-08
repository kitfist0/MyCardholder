package my.cardholder.ui.card.labels

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import my.cardholder.data.CardRepository
import my.cardholder.data.LabelDao
import my.cardholder.data.SettingsDataStore
import my.cardholder.data.model.Label
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardLabelsViewModel @Inject constructor(
    labelDao: LabelDao,
) : BaseViewModel() {

    private val _state = MutableStateFlow<CardLabelsState>(CardLabelsState.Empty())
    val state = _state.asStateFlow()

    init {
        labelDao.getLabels()
            .onEach { labels ->
                _state.value = if (labels.isNotEmpty()) {
                    CardLabelsState.Success(labels)
                } else {
                    CardLabelsState.Empty()
                }
            }
            .launchIn(viewModelScope)
    }

    fun onLabelClicked(label: Label) {
    }

    fun onAddLabelFabClicked() {
    }
}
