package my.cardholder.ui.coffee

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.BillingFlowParams
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import my.cardholder.util.PlayBillingWrapper
import my.cardholder.databinding.DialogCoffeeBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.util.ext.collectWhenStarted
import javax.inject.Inject

@AndroidEntryPoint
class CoffeeDialog : BaseDialogFragment<DialogCoffeeBinding>(
    DialogCoffeeBinding::inflate
) {

    @Inject
    lateinit var playBillingWrapper: PlayBillingWrapper

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
            state.launchBillingFlowRequest?.let { launchBillingFlow(it) }
        }
    }

    private fun launchBillingFlow(billingFlowParams: BillingFlowParams) {
        viewModel.onBillingFlowLaunched()
        lifecycleScope.launch {
            playBillingWrapper.getClientOrNull()?.let { billingClient ->
                val result = billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
                viewModel.onBillingFlowResult(result)
            }
        }
    }
}
