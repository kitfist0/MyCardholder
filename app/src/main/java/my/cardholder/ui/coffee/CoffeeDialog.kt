package my.cardholder.ui.coffee

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import my.cardholder.databinding.DialogCoffeeBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.util.ext.collectWhenStarted
import javax.inject.Inject

@AndroidEntryPoint
class CoffeeDialog : BaseDialogFragment<DialogCoffeeBinding>(
    DialogCoffeeBinding::inflate
) {

    @Inject
    lateinit var coffeePurchaseFlowLauncher: CoffeePurchaseFlowLauncher

    private val listAdapter by lazy(LazyThreadSafetyMode.NONE) {
        CoffeeAdapter {
            viewModel.onCoffeeClicked(it)
        }
    }

    override val viewModel: CoffeeViewModel by viewModels()

    override fun initViews() {
        binding.coffeeRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            listAdapter.submitList(state.coffees)
            state.launchCoffeePurchaseFlow?.let { launchCoffeePurchaseFlow(it) }
        }
    }

    private fun launchCoffeePurchaseFlow(productId: String) {
        lifecycleScope.launch {
            coffeePurchaseFlowLauncher.startPurchase(requireActivity(), productId)
                .onSuccess { viewModel.onCoffeePurchaseFlowStartedSuccessfully() }
                .onFailure { viewModel.onCoffeePurchaseFlowStartedWithError(it) }
        }
    }
}
