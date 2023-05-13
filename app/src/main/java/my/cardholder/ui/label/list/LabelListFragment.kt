package my.cardholder.ui.label.list

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentLabelListBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.ui.card.adapter.LabelAdapter
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class LabelListFragment : BaseFragment<FragmentLabelListBinding>(
    FragmentLabelListBinding::inflate
) {

    override val viewModel: LabelListViewModel by viewModels()

    private val listAdapter by lazy(LazyThreadSafetyMode.NONE) {
        LabelAdapter(
            onItemClick = { label ->
                viewModel.onLabelClicked(label)
            }
        )
    }

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            labelListRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
            }
            labelListAddLabelFab.setOnClickListener {
                viewModel.onAddLabelFabClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is LabelListState.Empty -> {
                    binding.labelListAddLabelFab.isVisible = false
                    binding.labelListEmptyMessageText.setText(state.messageRes)
                    listAdapter.submitList(null)
                }
                is LabelListState.Success -> {
                    binding.labelListAddLabelFab.isVisible = true
                    binding.labelListEmptyMessageText.text = null
                    listAdapter.submitList(state.labels)
                }
            }
        }
    }
}
