package my.cardholder.util.ext

import androidx.fragment.app.Fragment
import my.cardholder.util.Text

fun Fragment.getActionBarSize(): Int {
    val typedArray = requireContext().theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
    val actionBarSize = typedArray.getDimensionPixelSize(0, -1)
    typedArray.recycle()
    return actionBarSize
}

fun Fragment.textToString(text: Text): String {
    return when (text) {
        is Text.Resource -> getString(text.resId)
        is Text.ResourceAndParams -> getString(text.resId, *text.params.toTypedArray())
        is Text.Simple -> text.text
    }
}
