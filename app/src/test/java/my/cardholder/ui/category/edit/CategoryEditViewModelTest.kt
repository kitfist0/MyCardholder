package my.cardholder.ui.category.edit

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
import my.cardholder.R
import my.cardholder.data.CategoryRepository
import my.cardholder.data.model.Category
import my.cardholder.ui.base.BaseEvent
import my.cardholder.util.Text
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryEditViewModelTest {

    private companion object {
        const val CATEGORY_ID = 7L
        const val OTHER_CATEGORY_ID = 3L
    }

    private val categoryRepository = mockk<CategoryRepository>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { categoryRepository.getCategoryNameById(CATEGORY_ID) } returns "Shops"
        coEvery { categoryRepository.getCategoryIdByName(any()) } returns null
        coEvery { categoryRepository.getCategoryIdByName("Shops") } returns CATEGORY_ID
        coEvery { categoryRepository.getCategoryIdByName("Taken") } returns OTHER_CATEGORY_ID
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `renaming to name of another category shows error and stays on screen`() = runTest {
        val viewModel = createViewModel(CATEGORY_ID)

        viewModel.baseEvents.test {
            viewModel.onCategoryNameChanged("Taken")
            viewModel.onOkFabClicked()

            assertEquals(BaseEvent.SnackMessage(Text.Resource(R.string.category_edit_name_is_taken_error_message)), awaitItem())
            expectNoEvents()
        }
        coVerify(exactly = 0) { categoryRepository.upsertCategoryIfCategoryNameIsNew(any(), any()) }
    }

    @Test
    fun `creating category with name of existing one shows error`() = runTest {
        val viewModel = createViewModel(Category.NEW_CATEGORY_ID)

        viewModel.baseEvents.test {
            viewModel.onCategoryNameChanged("Shops")
            viewModel.onOkFabClicked()

            assertEquals(BaseEvent.SnackMessage(Text.Resource(R.string.category_edit_name_is_taken_error_message)), awaitItem())
            expectNoEvents()
        }
        coVerify(exactly = 0) { categoryRepository.upsertCategoryIfCategoryNameIsNew(any(), any()) }
    }

    @Test
    fun `saving category with its own name closes screen`() = runTest {
        val viewModel = createViewModel(CATEGORY_ID)

        viewModel.baseEvents.test {
            viewModel.onCategoryNameChanged("Shops")
            viewModel.onOkFabClicked()

            assertEquals(BaseEvent.NavigateUp, awaitItem())
        }
    }

    @Test
    fun `renaming to new name renames category and closes screen`() = runTest {
        val viewModel = createViewModel(CATEGORY_ID)

        viewModel.baseEvents.test {
            viewModel.onCategoryNameChanged("Groceries")
            viewModel.onOkFabClicked()

            assertEquals(BaseEvent.NavigateUp, awaitItem())
        }
        coVerify { categoryRepository.upsertCategoryIfCategoryNameIsNew(CATEGORY_ID, "Groceries") }
    }

    private fun createViewModel(categoryId: Long) = CategoryEditViewModel(
        SavedStateHandle(mapOf("category_id" to categoryId)),
        categoryRepository,
    )
}
