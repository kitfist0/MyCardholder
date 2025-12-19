package my.cardholder.ui.payment.options

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.cardholder.BuildConfig
import my.cardholder.data.model.PaymentOption
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class PaymentOptionsViewModel @Inject constructor() : BaseViewModel() {

    private val _state = MutableStateFlow<PaymentOptionsState>(PaymentOptionsState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Simulate loading payment options
            delay(1000)
            onPaymentOptionClicked(PaymentOption.MONTHLY)
        }
    }

    fun onPaymentOptionClicked(paymentOption: PaymentOption) {
        _state.value = PaymentOptionsState.Selection(
            PaymentOption.entries.map {
                PaymentOptionState(
                    paymentOption = it,
                    costOfPayment = when (it) {
                        PaymentOption.ANNUAL -> "99$"
                        PaymentOption.MONTHLY -> "10$"
                    },
                    isSelected = it == paymentOption,
                )
            }
        )
    }

    fun onPolicyTextClicked() {
        startActivity("android.intent.action.VIEW", BuildConfig.WEB_PAGE_POLICY)
    }

    fun onSubscribeFabClicked() {
        val prevState = _state.value
        _state.value = PaymentOptionsState.Loading
        viewModelScope.launch {
            delay(1000)
            _state.value = prevState
            navigate(
                if (Random.nextBoolean()) {
                    PaymentOptionsFragmentDirections.fromPaymentOptionsToPaymentError("Unknown error text")
                } else {
                    PaymentOptionsFragmentDirections.fromPaymentOptionsToPaymentDetails()
                }
            )
        }
    }
}
