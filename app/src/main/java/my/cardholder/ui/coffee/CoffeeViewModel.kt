package my.cardholder.ui.coffee

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.cardholder.data.CoffeeRepository
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.PlayBillingWrapper
import my.cardholder.util.Text
import my.cardholder.util.ext.isOk
import my.cardholder.util.ext.queryAndAcknowledgePurchasedProducts
import javax.inject.Inject

@HiltViewModel
class CoffeeViewModel @Inject constructor(
    private val coffeeRepository: CoffeeRepository,
    private val playBillingWrapper: PlayBillingWrapper,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        CoffeeState(
            coffees = emptyList(),
            launchCoffeePurchase = null,
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            coffeeRepository.initialize()
            updatePurchaseStatusOfCoffees()
        }
        coffeeRepository.coffees
            .onEach { coffees ->
                _state.update { it.copy(coffees = coffees) }
            }
            .launchIn(viewModelScope)
    }

    fun onCoffeeClicked(coffeeId: String) {
        _state.update { it.copy(launchCoffeePurchase = coffeeId) }
    }

    fun onCoffeePurchaseFlowStartedSuccessfully() {
        _state.update { it.copy(launchCoffeePurchase = null) }
        viewModelScope.launch {
            val purchasesResult = playBillingWrapper.purchasesResultChannel.receive()
            if (purchasesResult.billingResult.isOk()) {
                updatePurchaseStatusOfCoffees()
            }
        }
    }

    fun onCoffeePurchaseFlowStartedWithError(throwable: Throwable) {
        _state.update { it.copy(launchCoffeePurchase = null) }
        showSnack(Text.Simple(throwable.message.orEmpty()))
    }

    private suspend fun updatePurchaseStatusOfCoffees() {
        playBillingWrapper.getClient()
            .onSuccess { billingClient ->
                billingClient.queryAndAcknowledgePurchasedProducts().onSuccess { purchasedIds ->
                    coffeeRepository.updatePurchaseStatusOfCoffees(purchasedIds)
                }
            }
            .onFailure { showSnack(Text.Simple(it.message.orEmpty())) }
    }
}
