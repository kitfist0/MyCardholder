// package my.cardholder.ui.payment.details

// import dagger.hilt.android.lifecycle.HiltViewModel
// import kotlinx.coroutines.flow.MutableStateFlow
// import kotlinx.coroutines.flow.asStateFlow
// import kotlinx.coroutines.flow.update
// import my.cardholder.R
// // import my.cardholder.data.PaymentRepository
// import my.cardholder.data.model.PaymentOption
// import my.cardholder.ui.base.BaseViewModel
// import my.cardholder.util.Text
// import javax.inject.Inject

// @HiltViewModel
// class PaymentDetailsViewModel @Inject constructor(
//     paymentRepository: PaymentRepository,
// ) : BaseViewModel() {

//     private var _state = MutableStateFlow(
//         PaymentDetailsState(messageText = Text.Simple(""))
//     )
//     val state = _state.asStateFlow()

//     init {
//         paymentRepository.validPaymentOption?.let { paymentOption ->
//             val messageAboutPayment = Text.Resource(
//                 when (paymentOption) {
//                     PaymentOption.ANNUAL -> R.string.payment_details_message_about_annual_sub_text
//                     PaymentOption.MONTHLY -> R.string.payment_details_message_about_monthly_sub_text
//                 }
//             )
//             _state.update {
//                 it.copy(messageText = messageAboutPayment)
//             }
//         }
//     }

//     fun onOkFabClicked() {
//         navigateUp()
//     }
// }
