package my.cardholder.data.source

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import my.cardholder.data.model.Card
import my.cardholder.data.model.SupportedFormat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var cardDao: CardDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).build()
        cardDao = database.cardDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getNextPosition_isZeroWithoutCards() = runBlocking {
        assertEquals(0, cardDao.getNextPosition())
    }

    @Test
    fun getNextPosition_isAfterLastCardEvenIfEarlierCardsWereDeleted() = runBlocking {
        val firstCardId = cardDao.upsert(card(position = 0))
        cardDao.upsert(card(position = 1))
        cardDao.upsert(card(position = 2))
        cardDao.deleteCard(firstCardId)

        // The number of cards is 2 now, but position 2 is taken
        assertEquals(3, cardDao.getNextPosition())
    }

    @Test
    fun getCardsWithNamesLike_matchesEscapedWildcardsLiterally() = runBlocking {
        listOf("Discount 50%", "Sale 500", "my_shop", "myXshop", "back\\slash").forEachIndexed { position, name ->
            cardDao.upsert(card(position).copy(name = name))
        }

        assertEquals(listOf("Discount 50%"), cardDao.getCardsWithNamesLike("%50\\%%").map { it.name })
        assertEquals(listOf("my_shop"), cardDao.getCardsWithNamesLike("%my\\_shop%").map { it.name })
        assertEquals(listOf("back\\slash"), cardDao.getCardsWithNamesLike("%k\\\\s%").map { it.name })
    }

    private fun card(position: Int) = Card(
        id = Card.NEW_CARD_ID,
        name = "Card $position",
        position = position,
        isPinned = false,
        content = "12345",
        color = "#EF5350",
        format = SupportedFormat.QR_CODE,
        path = null,
        changedAt = 0L,
    )
}
