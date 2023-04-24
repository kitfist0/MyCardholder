package my.cardholder.ui.specs

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.cardholder.data.model.SupportedFormat
import my.cardholder.data.model.toSpec
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SpecsViewModel @Inject constructor() : BaseViewModel() {
    private val _specs = MutableStateFlow(
        SupportedFormat.values().map { format -> format.toSpec() }
    )
    val specs = _specs.asStateFlow()
}
