package my.cardholder.ui.specs

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.cardholder.R
import my.cardholder.data.model.SupportedFormat
import my.cardholder.data.model.isTwoDimensional
import my.cardholder.data.model.toSpec
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SpecsViewModel @Inject constructor() : BaseViewModel() {

    private val _state = MutableStateFlow(SpecsState(buildItems()))
    val state: StateFlow<SpecsState> = _state.asStateFlow()

    private fun buildItems(): List<SpecsItem> {
        val (twoDimensional, linear) = SupportedFormat.entries.partition { it.isTwoDimensional() }
        return buildList {
            add(SpecsItem.Header(R.string.specs_group_linear))
            linear.forEach { add(SpecsItem.Format(it.toSpec())) }
            add(SpecsItem.Header(R.string.specs_group_two_dimensional))
            twoDimensional.forEach { add(SpecsItem.Format(it.toSpec())) }
        }
    }
}
