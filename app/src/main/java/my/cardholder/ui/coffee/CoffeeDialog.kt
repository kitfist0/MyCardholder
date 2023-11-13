package my.cardholder.ui.coffee

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.DialogCoffeeBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.util.billing.BillingActivity
import my.cardholder.util.ext.collectWhenStarted

@AndroidEntryPoint
class CoffeeDialog : BaseDialogFragment<DialogCoffeeBinding>(
    DialogCoffeeBinding::inflate
) {

    private val listAdapter by lazy(LazyThreadSafetyMode.NONE) {
        CoffeeAdapter {
            (activity as? BillingActivity)?.purchaseProduct(it)
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
        }
    }
}
