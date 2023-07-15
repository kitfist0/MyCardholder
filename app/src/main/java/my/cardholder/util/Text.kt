package my.cardholder.util

import androidx.annotation.StringRes

sealed class Text {
    data class Resource(@StringRes val resId: Int) : Text()
    data class Simple(val text: String) : Text()
}
