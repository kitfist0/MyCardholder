package my.cardholder.ui.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
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
) : Fragment(), MenuProvider {

    abstract val viewModel: BaseViewModel

    open val menuRes: Int? = null

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
        menuRes?.let {
            val menuHost: MenuHost = requireActivity()
            menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        }
        return binding.root
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuRes?.let {
            menuInflater.inflate(it, menu)
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return viewModel.onMenuItemSelected(menuItem)
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
                is NavigateBack ->
                    findNavController().popBackStack()
                is StartActivity -> event.uriString
                    ?.let { uriString -> startActivity(Intent(event.action, Uri.parse(uriString))) }
                    ?: startActivity(Intent(event.action))
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
