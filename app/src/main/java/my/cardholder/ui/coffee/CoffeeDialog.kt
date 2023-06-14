package my.cardholder.ui.coffee

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import my.cardholder.databinding.DialogCoffeeBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.util.PurchaseFlowLauncher
import my.cardholder.util.ext.collectWhenStarted
import javax.inject.Inject

@AndroidEntryPoint
class CoffeeDialog : BaseDialogFragment<DialogCoffeeBinding>(
    DialogCoffeeBinding::inflate
) {

    @Inject
    lateinit var purchaseFlowLauncher: PurchaseFlowLauncher

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
            state.launchPurchaseFlow?.let { launchPurchaseFlow(it) }
        }
    }

    private fun launchPurchaseFlow(productId: String) {
        viewModel.onPurchaseFlowLaunched()
        lifecycleScope.launch {
            purchaseFlowLauncher.startPurchase(productId)
                .onSuccess { viewModel.onPurchaseFlowStartedSuccessfully() }
                .onFailure { viewModel.onPurchaseFlowStartedWithError(it) }
        }
    }
}
