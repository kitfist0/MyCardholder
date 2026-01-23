package my.cardholder.ui.settings

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.FragmentSettingsBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.setStartEndCompoundDrawables
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(
    FragmentSettingsBinding::inflate
) {

    private val listAdapter = SettingsItemsAdapter(
        onItemWithoutOptionsClicked = { itemId ->
            viewModel.onItemWithoutOptionsClicked(itemId)
        },
        onItemOptionClicked = { itemId, optionId ->
            viewModel.onItemOptionClicked(itemId, optionId)
        }
    )

    override val viewModel: SettingsViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets(bottom = false)
            settingsHeaderCard.setOnClickListener {
                listAdapter.collapseAll()
                // viewModel.onHeaderClicked()
            }
            settingsRecyclerView.apply {
                clipToPadding = false
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            listAdapter.submitList(state.settingsItems)
            setupHeader(state.headerState)
        }
    }

    private fun setupHeader(headerState: SettingsState.HeaderState) {
        val syncEnabled = headerState.cloudSyncEnabled
        binding.settingsHeaderText.apply {
            text = if (syncEnabled) {
                headerState.cloudName.orEmpty()
            } else {
                getString(R.string.settings_cloud_sync_switch_off_text)
            }
            setStartEndCompoundDrawables(
                endDrawableResId = if (syncEnabled) {
                    R.drawable.ic_cloud_on
                } else {
                    R.drawable.ic_cloud_off
                }
            )
        }
    }
}
