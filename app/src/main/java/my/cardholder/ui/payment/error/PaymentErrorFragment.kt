// package my.cardholder.ui.payment.error

// import androidx.fragment.app.viewModels
// import dagger.hilt.android.AndroidEntryPoint
// import my.cardholder.databinding.FragmentPaymentErrorBinding
// import my.cardholder.ui.base.BaseFragment
// import my.cardholder.util.ext.collectWhenStarted
// import my.cardholder.util.ext.textToString
// import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets
// import kotlin.getValue

// @AndroidEntryPoint
// class PaymentErrorFragment : BaseFragment<FragmentPaymentErrorBinding>(
//     FragmentPaymentErrorBinding::inflate
// ) {

//     override val viewModel: PaymentErrorViewModel by viewModels()

//     override fun initViews() {
//         with(binding) {
//             root.updateVerticalPaddingAfterApplyingWindowInsets()
//             paymentErrorFab.setOnClickListener {
//                 viewModel.onPaymentErrorFabClicked()
//             }
//         }
//     }

//     override fun collectData() {
//         collectWhenStarted(viewModel.state) { state ->
//             binding.paymentErrorMessageText.text = textToString(state.errorMessageText)
//         }
//     }
// }
