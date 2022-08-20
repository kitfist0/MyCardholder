package my.cardholder.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

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
