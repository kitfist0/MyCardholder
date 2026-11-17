package my.cardholder.data.source

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import my.cardholder.data.model.Card
import my.cardholder.data.model.Category
import my.cardholder.data.model.SupportedFormat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var cardDao: CardDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).build()
        categoryDao = database.categoryDao()
        cardDao = database.cardDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun renameCategoryAndUpdateItsCards_renamesCategoryAndChangesCategoryOfOnlyItsCards() = runBlocking {
        val renamedCategoryId = categoryDao.upsert(Category(name = "Shops"))
        val otherCategoryId = categoryDao.upsert(Category(name = "Other"))
        val cardInRenamedId = cardDao.upsert(card(categoryId = renamedCategoryId, changedAt = 100L))
        val cardInOtherId = cardDao.upsert(card(categoryId = otherCategoryId, changedAt = 100L))
        val cardWithoutCategoryId = cardDao.upsert(card(categoryId = null, changedAt = 100L))

        categoryDao.renameCategoryAndUpdateItsCards(renamedCategoryId, "Groceries", changedAt = 500L)

        assertEquals("Groceries", categoryDao.getCategoryById(renamedCategoryId)?.name)
        assertEquals(500L, cardDao.getCard(cardInRenamedId)?.categoryChangedAt)
        assertEquals("Card data keeps its change time", 100L, cardDao.getCard(cardInRenamedId)?.changedAt)
        assertEquals(100L, cardDao.getCard(cardInOtherId)?.categoryChangedAt)
        assertEquals(100L, cardDao.getCard(cardWithoutCategoryId)?.categoryChangedAt)
    }

    @Test
    fun renameCategoryAndUpdateItsCards_keepsChangeTimeLaterThanCurrentOne() = runBlocking {
        val categoryId = categoryDao.upsert(Category(name = "Shops"))
        // Changed on a device whose clock is ahead
        val cardId = cardDao.upsert(card(categoryId = categoryId, changedAt = 1_000L))

        categoryDao.renameCategoryAndUpdateItsCards(categoryId, "Groceries", changedAt = 500L)

        assertEquals(1_001L, cardDao.getCard(cardId)?.categoryChangedAt)
    }

    private fun card(categoryId: Long?, changedAt: Long) = Card(
        id = Card.NEW_CARD_ID,
        name = "Card",
        position = 0,
        isPinned = false,
        categoryId = categoryId,
        content = "12345",
        color = "#EF5350",
        format = SupportedFormat.QR_CODE,
        path = null,
        changedAt = changedAt,
    )
}
