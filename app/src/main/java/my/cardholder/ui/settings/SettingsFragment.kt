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

    private val listAdapter = SettingsMainAdapter(
        onOptionClicked = { itemId, optionId ->
            viewModel.onOptionClicked(itemId, optionId)
        }
    )

    override val viewModel: SettingsViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets(bottom = false)
            settingsHeaderCard.setOnClickListener {
                viewModel.onHeaderClicked()
            }
            settingsRecyclerView.apply {
                clipToPadding = false
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            listAdapter.submitList(state.settingsItems)
        }
    }
}
