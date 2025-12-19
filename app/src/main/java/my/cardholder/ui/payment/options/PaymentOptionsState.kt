package my.cardholder.ui.payment.options

import my.cardholder.data.model.PaymentOption

data class PaymentOptionState(
    val paymentOption: PaymentOption,
    val costOfPayment: String,
    val isSelected: Boolean,
)

sealed class PaymentOptionsState {
    data class Selection(val paymentOptionStates: List<PaymentOptionState>) : PaymentOptionsState()
    data object Loading : PaymentOptionsState()
}
