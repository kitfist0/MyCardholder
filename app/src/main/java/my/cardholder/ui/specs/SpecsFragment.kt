package my.cardholder.ui.specs

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import my.cardholder.databinding.FragmentSpecsBinding
import my.cardholder.ui.base.BaseFragment

@AndroidEntryPoint
class SpecsFragment : BaseFragment<FragmentSpecsBinding>(FragmentSpecsBinding::inflate) {

    override val viewModel: SpecsViewModel by viewModels()

    override fun initViews() {
        binding.specsRecyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    override fun collectData() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            val specs = viewModel.specs.first()
            binding.specsRecyclerView.adapter = SpecsAdapter(specs)
        }
    }
}
