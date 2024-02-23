package my.cardholder.data

import kotlinx.coroutines.flow.Flow
import my.cardholder.data.model.Coffee
import my.cardholder.data.source.CoffeeDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoffeeRepository @Inject constructor(
    private val coffeeDao: CoffeeDao,
) {

    private companion object {
        val COFFEE_IDS = listOf(
            "coffee.espresso",
            "coffee.cappuccino",
            "coffee.latte",
        )
    }

    val coffees: Flow<List<Coffee>> = coffeeDao.getCoffees()

    suspend fun updatePurchaseStatusOfCoffees(purchasedIds: List<String>) {
        val coffees = COFFEE_IDS.map { coffeeId ->
            Coffee(id = coffeeId, isPurchased = purchasedIds.contains(coffeeId))
        }
        coffeeDao.upsert(coffees)
    }
}
