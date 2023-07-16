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
        categoryDao.deleteCategoryById(categoryId)
    }

    suspend fun upsertCategoryIfCategoryNameIsNew(
        categoryId: Long = Category.NEW_CATEGORY_ID,
        categoryName: String,
    ): Long? {
        getCategoryIdByName(categoryName) ?: return null
        return categoryDao.upsert(Category(categoryId, categoryName))
    }
}
