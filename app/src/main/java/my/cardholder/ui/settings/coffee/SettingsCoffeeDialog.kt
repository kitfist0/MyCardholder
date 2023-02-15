package my.cardholder.ui.settings.coffee

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.DialogSettingsCoffeeBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.util.ext.collectWhenStarted

@AndroidEntryPoint
class SettingsCoffeeDialog : BaseDialogFragment<DialogSettingsCoffeeBinding>(
    DialogSettingsCoffeeBinding::inflate
) {

    private val listAdapter by lazy(LazyThreadSafetyMode.NONE) {
        SettingsCoffeeAdapter {
            viewModel.onCoffeeClicked(requireActivity(), it)
        }
    }

    override val viewModel: SettingsCoffeeViewModel by viewModels()

    override fun initViews() {
        binding.coffeeRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.coffees) { coffees ->
            listAdapter.submitList(coffees)
        }
    }
}
