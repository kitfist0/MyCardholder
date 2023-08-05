package my.cardholder.util.ext

import android.graphics.Rect
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import my.cardholder.util.Text

inline fun <reified T : ViewModel> Fragment.assistedViewModels(
    crossinline viewModelProducer: () -> T,
): Lazy<T> {
    return viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return viewModelProducer() as T
            }
        }
    }
}

fun Fragment.getStatusBarHeight(): Int {
    val rect = Rect()
    requireActivity().window.decorView.getWindowVisibleDisplayFrame(rect)
    return rect.top
}

fun Fragment.textToString(text: Text): String {
    return when (text) {
        is Text.Resource -> getString(text.resId)
        is Text.ResourceAndParams -> getString(text.resId, *text.params.toTypedArray())
        is Text.Simple -> text.text
    }
}
