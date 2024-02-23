package my.cardholder.ui.coffee

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import my.cardholder.data.CoffeeRepository
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CoffeeViewModel @Inject constructor(
    coffeeRepository: CoffeeRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        CoffeeState(coffees = emptyList())
    )
    val state = _state.asStateFlow()

    init {
        coffeeRepository.coffees
            .onEach { coffees ->
                _state.update { it.copy(coffees = coffees) }
            }
            .launchIn(viewModelScope)
    }
}
