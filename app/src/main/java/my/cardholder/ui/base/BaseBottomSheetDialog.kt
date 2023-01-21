package my.cardholder.ui.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

abstract class BaseBottomSheetDialog<out VB : ViewBinding>(
    private val inflate: Inflate<VB>,
) : BottomSheetDialogFragment() {

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
                is BaseEvent.Navigate -> event.extras
                    ?.let { extras -> findNavController().navigate(event.direction, extras) }
                    ?: findNavController().navigate(event.direction)
                is BaseEvent.NavigateUp ->
                    findNavController().navigateUp()
                is BaseEvent.SnackMessage ->
                    Snackbar.make(requireView(), event.message, Snackbar.LENGTH_LONG).show()
                is BaseEvent.StartActivity -> event.uriString
                    ?.let { uriString -> startActivity(Intent(event.action, Uri.parse(uriString))) }
                    ?: startActivity(Intent(event.action))
            }
        }
    }

    protected inline fun <T> Flow<T>.collectWhenStarted(crossinline action: (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            this@collectWhenStarted
                .onEach { action.invoke(it) }
                .collect()
        }
    }
}
