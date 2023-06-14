package my.cardholder.data

import kotlinx.coroutines.flow.Flow
import my.cardholder.data.model.Coffee
import my.cardholder.data.source.local.CoffeeDao
import my.cardholder.data.source.remote.PlayBillingApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoffeeRepository @Inject constructor(
    private val coffeeDao: CoffeeDao,
    private val playBillingApi: PlayBillingApi,
) {

    val coffees: Flow<List<Coffee>> = coffeeDao.getCoffees()

    suspend fun initialize() {
        if (coffeeDao.isEmpty()) {
            val coffees = playBillingApi.productIds.map { productId ->
                Coffee(id = productId, isPurchased = false)
            }
            coffeeDao.upsert(coffees)
        }
        updatePurchaseStatusOfCoffees()
    }

    suspend fun waitCoffeePurchaseResult(): Result<Boolean> {
        return playBillingApi.waitProductPurchaseResult()
            .onSuccess { updatePurchaseStatusOfCoffees() }
    }

    private suspend fun updatePurchaseStatusOfCoffees() {
        playBillingApi.getIdsOfPurchasedProducts()
            .onSuccess { productIds ->
                val coffees = productIds.map { productId ->
                    Coffee(id = productId, isPurchased = true)
                }
                coffeeDao.upsert(coffees)
            }
    }
}
