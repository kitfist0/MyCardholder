package my.cardholder.ui.payment.options

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentPaymentOptionsBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.makeItFiftyPercentMoreTransparentIf
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets
import kotlin.getValue

@AndroidEntryPoint
class PaymentOptionsFragment : BaseFragment<FragmentPaymentOptionsBinding>(
    FragmentPaymentOptionsBinding::inflate
) {

    override val viewModel: PaymentOptionsViewModel by viewModels()

    private val listAdapter = PaymentOptionAdapter {
        viewModel.onPaymentOptionClicked(it)
    }

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            binding.paymentOptionsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
            }
            paymentOptionsPolicyButton.setOnClickListener {
                viewModel.onPolicyButtonClicked()
            }
            paymentOptionsFab.setOnClickListener {
                viewModel.onSubscribeFabClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is PaymentOptionsState.Loading -> changeLoadingVisibility(true)
                is PaymentOptionsState.Selection -> {
                    listAdapter.submitList(state.paymentOptionStates)
                    changeLoadingVisibility(false)
                }
            }
        }
    }

    private fun changeLoadingVisibility(isLoading: Boolean) {
        with(binding) {
            paymentOptionsRecyclerView.apply {
                makeItFiftyPercentMoreTransparentIf(isLoading)
                isEnabled = !isLoading
            }
            paymentOptionsPolicyText.isVisible = !isLoading
            paymentOptionsLoadingProgress.isVisible = isLoading
            paymentOptionsFab.isVisible = !isLoading
        }
    }
}
