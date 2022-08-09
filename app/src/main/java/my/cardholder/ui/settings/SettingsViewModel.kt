package my.cardholder.ui.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : BaseViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Settings Fragment"
    }
    val text: Flow<String> = _text.asFlow()
}
