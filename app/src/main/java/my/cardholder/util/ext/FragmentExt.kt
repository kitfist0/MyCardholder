package my.cardholder.util.ext

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

fun Fragment.contextCompatDrawable(@DrawableRes drawableRes: Int): Drawable? {
    return ContextCompat.getDrawable(requireContext(), drawableRes)
}

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
