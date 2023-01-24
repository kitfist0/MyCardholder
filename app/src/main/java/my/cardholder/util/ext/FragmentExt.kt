package my.cardholder.util.ext

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

inline fun <T> Fragment.collectWhenStarted(
    flow: Flow<T>,
    crossinline action: (T) -> Unit,
) {
    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        flow.onEach { action.invoke(it) }
            .collect()
    }
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
