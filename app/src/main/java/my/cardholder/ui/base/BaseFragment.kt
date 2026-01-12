package my.cardholder.ui.base

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import my.cardholder.util.Text
import my.cardholder.util.ext.textToString

abstract class BaseFragment<out VB : ViewBinding>(
    private val inflate: Inflate<VB>,
) : Fragment() {

    abstract val viewModel: BaseViewModel

    abstract fun initViews()

    abstract fun collectData()

    protected var snackbar: Snackbar? = null

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
        dismissSnack()
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        collectData()
        collectAndHandleBaseEvents(viewModel)
    }

    fun showSnack(text: Text) {
        snackbar = Snackbar.make(requireView(), textToString(text), Snackbar.LENGTH_LONG)
        snackbar?.show()
    }

    fun showOkSnack(text: Text, actionCode: Int) {
        snackbar = Snackbar.make(requireView(), textToString(text), Snackbar.LENGTH_INDEFINITE)
        snackbar?.apply {
            setAction(android.R.string.ok) {
                viewModel.onOkSnackButtonClicked(actionCode)
                dismissSnack()
            }
            show()
        }
    }

    private fun dismissSnack() {
        snackbar?.dismiss()
        snackbar = null
    }
}
