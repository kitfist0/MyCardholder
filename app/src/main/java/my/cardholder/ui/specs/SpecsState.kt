package my.cardholder.ui.specs

import androidx.annotation.StringRes
import my.cardholder.data.model.FormatSpec

data class SpecsState(
    val items: List<SpecsItem>,
)

sealed interface SpecsItem {
    data class Header(@StringRes val titleRes: Int) : SpecsItem
    data class Format(val spec: FormatSpec) : SpecsItem
}
