package my.cardholder.ui.category.delete

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import my.cardholder.data.CategoryRepository
import my.cardholder.ui.base.BaseEvent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteCategoryViewModelTest {

    private companion object {
        const val CATEGORY_ID = 5L
    }

    private val categoryRepository = mockk<CategoryRepository>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { categoryRepository.getCategoryNameById(CATEGORY_ID) } returns "Groceries"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state contains category name`() = runTest {
        val viewModel = createViewModel(CATEGORY_ID)

        viewModel.state.test {
            assertEquals("Groceries", awaitItem().categoryName)
        }
    }

    @Test
    fun `clicking confirmation button deletes category and navigates to category list`() = runTest {
        val viewModel = createViewModel(CATEGORY_ID)

        viewModel.baseEvents.test {
            viewModel.onDeleteConfirmationButtonClicked()

            assertEquals(
                BaseEvent.Navigate(
                    DeleteCategoryDialogDirections.fromDeleteCategoryToCategoryList()
                ),
                awaitItem()
            )
        }
        coVerify { categoryRepository.deleteCategoryById(CATEGORY_ID) }
    }

    private fun createViewModel(categoryId: Long) = DeleteCategoryViewModel(
        SavedStateHandle(mapOf("category_id" to categoryId)),
        categoryRepository,
    )
}
