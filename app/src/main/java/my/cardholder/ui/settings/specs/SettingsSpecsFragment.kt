package my.cardholder.ui.settings.specs

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import my.cardholder.databinding.FragmentSettingsSpecsBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class SettingsSpecsFragment : BaseFragment<FragmentSettingsSpecsBinding>(
    FragmentSettingsSpecsBinding::inflate
) {

    override val viewModel: SettingsSpecsViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            specsRecyclerView.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    override fun collectData() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            val specs = viewModel.specs.first()
            binding.specsRecyclerView.adapter = SettingsSpecsAdapter(specs)
        }
    }
}
