package my.cardholder.data

import kotlinx.coroutines.flow.Flow
import my.cardholder.data.model.Coffee
import my.cardholder.data.source.local.CoffeeDao
import my.cardholder.data.source.remote.CoffeeApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoffeeRepository @Inject constructor(
    private val coffeeDao: CoffeeDao,
    private val coffeeApi: CoffeeApi,
) {

    private companion object {
        val COFFEE_IDS = listOf(
            "coffee.espresso",
            "coffee.cappuccino",
            "coffee.latte",
        )
    }

    val coffees: Flow<List<Coffee>> = coffeeDao.getCoffees()

    suspend fun initialize() {
        if (coffeeDao.isEmpty()) {
            val coffees = COFFEE_IDS.map { coffeeId ->
                Coffee(id = coffeeId, isPurchased = false)
            }
            coffeeDao.upsert(coffees)
        }
        updatePurchaseStatusOfCoffees()
    }

    suspend fun waitCoffeePurchaseResult(): Result<Unit> {
        return coffeeApi.waitCoffeePurchaseResult()
            .onSuccess { updatePurchaseStatusOfCoffees() }
    }

    private suspend fun updatePurchaseStatusOfCoffees() {
        coffeeApi.getIdsOfPurchasedCoffees()
            .onSuccess { coffeeIds ->
                val coffees = coffeeIds.map { coffeeId ->
                    Coffee(id = coffeeId, isPurchased = true)
                }
                coffeeDao.upsert(coffees)
            }
    }
}
