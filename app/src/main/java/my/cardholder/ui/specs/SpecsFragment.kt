package my.cardholder.ui.specs

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentSpecsBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class SpecsFragment : BaseFragment<FragmentSpecsBinding>(
    FragmentSpecsBinding::inflate
) {

    override val viewModel: SpecsViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            specsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            specsRecyclerView.adapter = SpecsAdapter()
        }
    }

    override fun collectData() {
        viewLifecycleOwner.collectWhenStarted(viewModel.state) { state ->
            (binding.specsRecyclerView.adapter as SpecsAdapter).submitList(state.items)
        }
    }
}
