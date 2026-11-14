package my.cardholder.data

import kotlinx.coroutines.flow.Flow
import my.cardholder.data.model.Category
import my.cardholder.data.model.CategoryAndCards
import my.cardholder.data.source.CategoryDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
) {

    val categoriesAndCards: Flow<List<CategoryAndCards>>
        get() = categoryDao.getCategoriesAndCards()

    suspend fun getCategoryAndCards(categoryName: String): CategoryAndCards? {
        return categoryDao.getCategoryAndCards(categoryName)
    }

    suspend fun getCategoryNameById(categoryId: Long): String? {
        return categoryDao.getCategoryById(categoryId)?.name
    }

    suspend fun getCategoryIdByName(categoryName: String): Long? {
        return categoryDao.getCategoryByName(categoryName)?.id
    }

    suspend fun getCategoryNames(): List<String> {
        return categoryDao.getCategoryNames()
    }

    suspend fun deleteCategoryById(categoryId: Long) {
        categoryDao.deleteCategoryAndUpdateItsCards(categoryId, System.currentTimeMillis())
    }

    /**
     * Creates a new category or renames the existing one with [categoryId].
     * Does nothing if a category with [categoryName] already exists and returns its id.
     */
    suspend fun upsertCategoryIfCategoryNameIsNew(
        categoryId: Long = Category.NEW_CATEGORY_ID,
        categoryName: String,
    ): Long {
        getCategoryIdByName(categoryName)?.let { existingCategoryId -> return existingCategoryId }
        return if (categoryId == Category.NEW_CATEGORY_ID) {
            categoryDao.upsert(Category(categoryId, categoryName))
        } else {
            categoryDao.renameCategoryAndUpdateItsCards(categoryId, categoryName, System.currentTimeMillis())
            categoryId
        }
    }
}
