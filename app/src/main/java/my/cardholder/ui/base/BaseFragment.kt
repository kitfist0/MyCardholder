package my.cardholder.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment<out VB : ViewBinding>(
    private val inflate: Inflate<VB>,
) : Fragment() {

    abstract val viewModel: BaseViewModel

    abstract fun initViews()

    abstract fun collectData()

    private var _binding: VB? = null

    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        collectData()
        viewModel.baseEvents.collectWhenStarted { event ->
            when (event) {
                is SnackMessage ->
                    Snackbar.make(requireView(), event.message, Snackbar.LENGTH_LONG).show()
                is Navigate -> event.extras
                    ?.let { extras -> findNavController().navigate(event.direction, extras) }
                    ?: findNavController().navigate(event.direction)
            }
        }
    }

    protected fun <T> Flow<T>.collectWhenStarted(action: (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            this@collectWhenStarted
                .onEach { action.invoke(it) }
                .collect()
        }
    }
}
