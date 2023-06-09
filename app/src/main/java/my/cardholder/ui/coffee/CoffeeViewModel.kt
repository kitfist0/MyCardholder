package my.cardholder.ui.coffee

import android.app.Activity
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import my.cardholder.data.source.local.CoffeeDao
import my.cardholder.data.source.remote.PlayBillingApi
import my.cardholder.data.model.Coffee
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CoffeeViewModel @Inject constructor(
    private val coffeeDao: CoffeeDao,
    private val playBillingApi: PlayBillingApi,
) : BaseViewModel() {

    val coffees = coffeeDao.getCoffees()

    init {
        viewModelScope.launch {
            if (coffeeDao.isEmpty()) {
                playBillingApi.productIds.onEach { productId ->
                    coffeeDao.insert(
                        Coffee(id = productId, isPurchased = false)
                    )
                }
            }
            playBillingApi.getIdsOfPurchasedProducts()
                .onSuccess { purchasedIds ->
                    purchasedIds.onEach { onCoffeePurchased(it) }
                }
        }
    }

    fun onCoffeeClicked(activity: Activity, productId: String) {
        viewModelScope.launch {
            playBillingApi.purchaseProduct(activity, productId)
                .onSuccess { onCoffeePurchased(it) }
                .onFailure { showSnack(it.message.orEmpty()) }
        }
    }

    private suspend fun onCoffeePurchased(productId: String) {
        coffeeDao.insert(
            Coffee(id = productId, isPurchased = true)
        )
    }
}
