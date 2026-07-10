package my.cardholder.data

import app.cash.turbine.test
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import my.cardholder.data.model.Category
import my.cardholder.data.model.CategoryAndCards
import my.cardholder.data.source.CategoryDao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryRepositoryTest {

    private val categoryDao = mockk<CategoryDao>()
    private val categoryRepository = CategoryRepository(categoryDao)

    @Test
    fun `categoriesAndCards returns flow from dao`() = runTest {
        val categoriesAndCards = listOf(
            CategoryAndCards(Category(1, "Cat 1"), emptyList()),
            CategoryAndCards(Category(2, "Cat 2"), emptyList())
        )
        every { categoryDao.getCategoriesAndCards() } returns flowOf(categoriesAndCards)

        categoryRepository.categoriesAndCards.test {
            assertEquals(categoriesAndCards, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getCategoryAndCards returns data from dao`() = runTest {
        val categoryName = "Test"
        val expected = CategoryAndCards(Category(1, categoryName), emptyList())
        coEvery { categoryDao.getCategoryAndCards(categoryName) } returns expected

        val result = categoryRepository.getCategoryAndCards(categoryName)

        assertEquals(expected, result)
    }

    @Test
    fun `getCategoryNameById returns name when category exists`() = runTest {
        val categoryId = 1L
        val category = Category(categoryId, "Test Category")
        coEvery { categoryDao.getCategoryById(categoryId) } returns category

        val result = categoryRepository.getCategoryNameById(categoryId)

        assertEquals("Test Category", result)
    }

    @Test
    fun `getCategoryNameById returns null when category does not exist`() = runTest {
        val categoryId = 1L
        coEvery { categoryDao.getCategoryById(categoryId) } returns null

        val result = categoryRepository.getCategoryNameById(categoryId)

        assertNull(result)
    }

    @Test
    fun `getCategoryIdByName returns id when category exists`() = runTest {
        val categoryName = "Test"
        val category = Category(5L, categoryName)
        coEvery { categoryDao.getCategoryByName(categoryName) } returns category

        val result = categoryRepository.getCategoryIdByName(categoryName)

        assertEquals(5L, result)
    }

    @Test
    fun `getCategoryNames returns list from dao`() = runTest {
        val expectedNames = listOf("A", "B", "C")
        coEvery { categoryDao.getCategoryNames() } returns expectedNames

        val result = categoryRepository.getCategoryNames()

        assertEquals(expectedNames, result)
    }

    @Test
    fun `deleteCategoryById calls dao delete`() = runTest {
        val categoryId = 10L
        coEvery { categoryDao.deleteCategoryById(categoryId) } just Runs

        categoryRepository.deleteCategoryById(categoryId)

        coVerify { categoryDao.deleteCategoryById(categoryId) }
    }

    @Test
    fun `upsertCategoryIfCategoryNameIsNew returns existing id if found`() = runTest {
        val categoryName = "Existing"
        coEvery { categoryDao.getCategoryByName(categoryName) } returns Category(123L, categoryName)

        val resultId = categoryRepository.upsertCategoryIfCategoryNameIsNew(categoryName = categoryName)

        assertEquals(123L, resultId)
        coVerify(exactly = 0) { categoryDao.upsert(any()) }
    }

    @Test
    fun `upsertCategoryIfCategoryNameIsNew calls upsert if category name is new`() = runTest {
        val categoryName = "New Category"
        coEvery { categoryDao.getCategoryByName(categoryName) } returns null
        coEvery { categoryDao.upsert(any()) } returns 456L

        val resultId = categoryRepository.upsertCategoryIfCategoryNameIsNew(categoryName = categoryName)

        assertEquals(456L, resultId)
        coVerify { categoryDao.upsert(Category(id = Category.NEW_CATEGORY_ID, name = categoryName)) }
    }
}
