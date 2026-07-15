package my.cardholder.data

import app.cash.turbine.test
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import my.cardholder.data.model.Coffee
import my.cardholder.data.source.CoffeeDao
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoffeeRepositoryTest {

    private val coffeeDao = mockk<CoffeeDao>()
    private lateinit var coffeeRepository: CoffeeRepository

    @Before
    fun setUp() {
        every { coffeeDao.getCoffees() } returns emptyFlow()
        coffeeRepository = CoffeeRepository(coffeeDao)
    }

    @Test
    fun `coffees returns flow from dao`() = runTest {
        val expectedCoffees = Coffee.COFFEE_IDS.map { Coffee(id = it, isPurchased = false) }
        every { coffeeDao.getCoffees() } returns flowOf(expectedCoffees)

        // Re-initialize to pick up the new flow if necessary, 
        // but coffees is a val, so it's assigned once during init.
        // Actually, in the current implementation of CoffeeRepository:
        // val coffees: Flow<List<Coffee>> = coffeeDao.getCoffees()
        // It means it calls getCoffees() once.

        val repo = CoffeeRepository(coffeeDao)

        repo.coffees.test {
            assertEquals(expectedCoffees, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `updatePurchaseStatusOfCoffees updates all coffees in dao`() = runTest {
        val purchasedIds = listOf("coffee.espresso", "coffee.latte")
        coEvery { coffeeDao.upsert(any()) } just Runs

        coffeeRepository.updatePurchaseStatusOfCoffees(purchasedIds)

        coVerify {
            coffeeDao.upsert(match { coffees ->
                coffees.size == Coffee.COFFEE_IDS.size &&
                        coffees.find { it.id == "coffee.espresso" }?.isPurchased == true &&
                        coffees.find { it.id == "coffee.latte" }?.isPurchased == true &&
                        coffees.find { it.id == "coffee.cappuccino" }?.isPurchased == false
            })
        }
    }
}
