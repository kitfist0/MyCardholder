package my.cardholder.ui.coffee

import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.cardholder.data.CoffeeRepository
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CoffeeViewModel @Inject constructor(
    private val coffeeRepository: CoffeeRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        CoffeeState(
            coffees = emptyList(),
            launchBillingFlowRequest = null,
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            coffeeRepository.initialize()
        }
        coffeeRepository.coffees
            .onEach { coffees ->
                _state.update { it.copy(coffees = coffees) }
            }
            .launchIn(viewModelScope)
    }

    fun onCoffeeClicked(coffeeId: String) {
        viewModelScope.launch {
            coffeeRepository.getCoffeeBillingFlowParams(coffeeId)
                .onSuccess { billingFlowParams ->
                    _state.update { it.copy(launchBillingFlowRequest = billingFlowParams) }
                }
                .onFailure { showSnack(it.message.orEmpty()) }
        }
    }

    fun onBillingFlowLaunched() {
        _state.update { it.copy(launchBillingFlowRequest = null) }
    }

    fun onBillingFlowResult(billingResult: BillingResult) {
        viewModelScope.launch {
            coffeeRepository.processCoffeeBillingFlowResult(billingResult)
                .onFailure { showSnack(it.message.orEmpty()) }
        }
    }
}
