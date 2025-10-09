package my.cardholder.ui.settings

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentSettingsBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(
    FragmentSettingsBinding::inflate
) {

    private val listAdapter = SettingsAdapter(
        onItemClicked = { viewModel.onListItemClicked(it) }
    )

    override val viewModel: SettingsViewModel by viewModels()

    override fun initViews() {
        binding.settingsRecyclerView.apply {
            clipToPadding = false
            setHasFixedSize(true)
            updateVerticalPaddingAfterApplyingWindowInsets(bottom = false)
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            listAdapter.submitList(state.settingsItems)
        }
    }
}
