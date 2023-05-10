package my.cardholder.ui.card.labels

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardLabelsBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.ui.card.adapter.LabelAdapter
import my.cardholder.util.ext.collectWhenStarted

@AndroidEntryPoint
class CardLabelsFragment : BaseFragment<FragmentCardLabelsBinding>(
    FragmentCardLabelsBinding::inflate
) {

    override val viewModel: CardLabelsViewModel by viewModels()

    private val listAdapter by lazy(LazyThreadSafetyMode.NONE) {
        LabelAdapter(
            onItemClick = { label ->
                viewModel.onLabelClicked(label)
            }
        )
    }

    override fun initViews() {
        with(binding) {
            cardLabelsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
            }
            cardLabelsAddLabelFab.setOnClickListener {
                viewModel.onAddLabelFabClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CardLabelsState.Empty -> {
                    binding.cardLabelsAddLabelFab.isVisible = false
                    binding.cardLabelsEmptyListMessageText.setText(state.messageRes)
                    listAdapter.submitList(null)
                }
                is CardLabelsState.Success -> {
                    binding.cardLabelsAddLabelFab.isVisible = true
                    binding.cardLabelsEmptyListMessageText.text = null
                    listAdapter.submitList(state.labels)
                }
            }
        }
    }
}
