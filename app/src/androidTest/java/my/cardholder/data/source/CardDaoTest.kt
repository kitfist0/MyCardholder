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
