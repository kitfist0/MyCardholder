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
import javax.inject.Inject

@HiltViewModel
class CoffeeViewModel @Inject constructor(
    private val coffeeRepository: CoffeeRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        CoffeeState(
            coffees = emptyList(),
            launchCoffeePurchaseFlow = null,
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
        _state.update { it.copy(launchCoffeePurchaseFlow = coffeeId) }
    }

    fun onCoffeePurchaseFlowStartedSuccessfully() {
        _state.update { it.copy(launchCoffeePurchaseFlow = null) }
        viewModelScope.launch {
            coffeeRepository.waitCoffeePurchaseResult()
                .onFailure { showSnack(it.message.orEmpty()) }
        }
    }

    fun onCoffeePurchaseFlowStartedWithError(throwable: Throwable) {
        _state.update { it.copy(launchCoffeePurchaseFlow = null) }
        showSnack(throwable.message.orEmpty())
    }
}
