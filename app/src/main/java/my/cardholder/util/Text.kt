package my.cardholder.util

import androidx.annotation.StringRes

sealed class Text {
    data class Resource(@StringRes val resId: Int) : Text()
    data class ResourceAndParams(@StringRes val resId: Int, val params: List<Any>) : Text()
    data class Simple(val text: String) : Text()
}
