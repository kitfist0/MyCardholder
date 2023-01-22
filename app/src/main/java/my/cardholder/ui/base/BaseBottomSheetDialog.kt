package my.cardholder.ui.base

import android.os.Bundle
import android.view.*
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

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
        collectAndHandleBaseEvents(viewModel)
    }
}
