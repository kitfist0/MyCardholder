package my.cardholder.data

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import my.cardholder.data.model.Card
import my.cardholder.data.model.CardAndCategory
import my.cardholder.data.model.CardVersion
import my.cardholder.data.model.Category
import my.cardholder.data.model.DeletedCard
import my.cardholder.data.model.SupportedFormat
import my.cardholder.data.model.SyncCard
import my.cardholder.data.model.SyncSnapshot
import my.cardholder.data.source.CardDao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SyncRepositoryTest {

    @Test
    fun `newer remote card replaces local card and keeps remote version`() = runTest {
        val device = Device()
        device.addCard(syncId = "a", name = "Old", changedAt = 100L)

        device.repository.mergeSnapshot(snapshotOf(syncCard(syncId = "a", name = "New", changedAt = 200L, categoryName = "Shops")))

        val card = device.cardDao.getCardBySyncId("a")!!
        assertEquals("New", card.name)
        assertEquals(200L, card.changedAt)
        assertEquals("Shops", device.categoryNameOf(card.categoryId))
    }

    @Test
    fun `older remote card does not replace local card`() = runTest {
        val device = Device()
        device.addCard(syncId = "a", name = "Local", changedAt = 300L)

        device.repository.mergeSnapshot(snapshotOf(syncCard(syncId = "a", name = "Remote", changedAt = 200L)))

        assertEquals("Local", device.cardDao.getCardBySyncId("a")!!.name)
    }

    @Test
    fun `unknown remote card is inserted`() = runTest {
        val device = Device()

        device.repository.mergeSnapshot(snapshotOf(syncCard(syncId = "a", name = "Remote", changedAt = 200L)))

        val card = device.cardDao.getCardBySyncId("a")!!
        assertEquals("Remote", card.name)
        assertEquals(200L, card.changedAt)
    }

    @Test
    fun `remote deletion removes card changed before it`() = runTest {
        val device = Device()
        device.addCard(syncId = "a", changedAt = 100L)

        device.repository.mergeSnapshot(SyncSnapshot(emptyList(), listOf(DeletedCard("a", 200L))))

        assertNull(device.cardDao.getCardBySyncId("a"))
        assertEquals(DeletedCard("a", 200L), device.cardDao.getDeletedCard("a"))
    }

    @Test
    fun `card changed after remote deletion survives`() = runTest {
        val device = Device()
        device.addCard(syncId = "a", changedAt = 300L)

        device.repository.mergeSnapshot(SyncSnapshot(emptyList(), listOf(DeletedCard("a", 200L))))

        assertNotNull(device.cardDao.getCardBySyncId("a"))
        assertNull(device.cardDao.getDeletedCard("a"))
    }

    @Test
    fun `remote card changed before local deletion stays deleted`() = runTest {
        val device = Device()
        device.cardDao.insertDeletedCard(DeletedCard("a", 300L))

        device.repository.mergeSnapshot(snapshotOf(syncCard(syncId = "a", changedAt = 200L)))

        assertNull(device.cardDao.getCardBySyncId("a"))
        assertNotNull(device.cardDao.getDeletedCard("a"))
    }

    @Test
    fun `remote card changed after local deletion is restored`() = runTest {
        val device = Device()
        device.cardDao.insertDeletedCard(DeletedCard("a", 100L))

        device.repository.mergeSnapshot(snapshotOf(syncCard(syncId = "a", changedAt = 200L)))

        assertNotNull(device.cardDao.getCardBySyncId("a"))
        assertNull(device.cardDao.getDeletedCard("a"))
    }

    @Test
    fun `legacy card is inserted only if there is no such card`() = runTest {
        val device = Device()
        device.addCard(syncId = "a", name = "Existing", content = "1")

        device.repository.mergeSnapshot(
            snapshotOf(
                syncCard(syncId = null, name = "Existing", content = "1"),
                syncCard(syncId = null, name = "New", content = "2"),
            )
        )

        val cards = device.cardDao.cards.value.values
        assertEquals(2, cards.size)
        val newCard = cards.single { it.name == "New" }
        assertNotEquals("", newCard.syncId)
    }

    @Test
    fun `same card created on another device adopts its sync id`() = runTest {
        val device = Device()
        device.addCard(syncId = "local", name = "Shop", content = "1", changedAt = 100L)

        device.repository.mergeSnapshot(snapshotOf(syncCard(syncId = "remote", name = "Shop", content = "1", changedAt = 50L)))

        val card = device.cardDao.cards.value.values.single()
        assertEquals("remote", card.syncId)
        assertEquals(100L, card.changedAt)
    }

    @Test
    fun `checksum depends on versions but not on order`() {
        val repository = Device().repository
        val first = syncCard(syncId = "a", changedAt = 1L)
        val second = syncCard(syncId = "b", changedAt = 2L)

        assertEquals(
            repository.calculateChecksum(snapshotOf(first, second)),
            repository.calculateChecksum(snapshotOf(second, first)),
        )
        assertNotEquals(
            repository.calculateChecksum(snapshotOf(first, second)),
            repository.calculateChecksum(snapshotOf(first, second.copy(changedAt = 3L))),
        )
        assertNotEquals(
            repository.calculateChecksum(snapshotOf(first)),
            repository.calculateChecksum(SyncSnapshot(emptyList(), listOf(DeletedCard("a", 1L)))),
        )
    }

    @Test
    fun `local checksum matches checksum of local snapshot`() = runTest {
        val device = Device()
        device.addCard(syncId = "a", changedAt = 100L)
        device.cardDao.insertDeletedCard(DeletedCard("b", 200L))

        assertEquals(
            device.repository.calculateChecksum(device.repository.getLocalSnapshot()),
            device.repository.getLocalChecksum(),
        )
    }

    @Test
    fun `devices converge after concurrent changes`() = runTest {
        val phone = Device()
        val tablet = Device()
        listOf(phone, tablet).forEach { device ->
            device.addCard(syncId = "edited", name = "Original", content = "1", changedAt = 100L)
            device.addCard(syncId = "deleted", name = "Deleted", content = "2", changedAt = 100L)
            device.addCard(syncId = "edited twice", name = "Original", content = "3", changedAt = 100L)
        }
        phone.updateCard("edited", name = "Phone edit", changedAt = 200L)
        phone.updateCard("edited twice", name = "Phone edit", changedAt = 200L)
        phone.addCard(syncId = "phone new", name = "Phone card", content = "4", changedAt = 210L)
        tablet.deleteCard("deleted", deletedAt = 220L)
        tablet.updateCard("edited twice", name = "Tablet edit", changedAt = 230L)
        tablet.addCard(syncId = "tablet new", name = "Tablet card", content = "5", changedAt = 240L)

        // Phone uploads first, tablet merges and uploads the result, phone merges it back
        tablet.repository.mergeSnapshot(phone.repository.getLocalSnapshot())
        phone.repository.mergeSnapshot(tablet.repository.getLocalSnapshot())

        assertEquals(phone.repository.getLocalChecksum(), tablet.repository.getLocalChecksum())
        assertEquals(phone.sortedSnapshot(), tablet.sortedSnapshot())
        val cards = phone.cardDao.cards.value.values.associateBy { it.syncId }
        assertEquals(setOf("edited", "edited twice", "phone new", "tablet new"), cards.keys)
        assertEquals("Phone edit", cards.getValue("edited").name)
        assertEquals("Tablet edit", cards.getValue("edited twice").name)
        assertEquals(DeletedCard("deleted", 220L), phone.cardDao.getDeletedCard("deleted"))
    }

    @Test
    fun `merge is idempotent`() = runTest {
        val device = Device()
        device.addCard(syncId = "a", changedAt = 100L)
        val remote = SyncSnapshot(
            cards = listOf(syncCard(syncId = "b", changedAt = 200L)),
            deletedCards = listOf(DeletedCard("a", 300L)),
        )

        device.repository.mergeSnapshot(remote)
        val checksum = device.repository.getLocalChecksum()
        device.repository.mergeSnapshot(remote)

        assertEquals(checksum, device.repository.getLocalChecksum())
        assertEquals(1, device.cardDao.cards.value.size)
    }

    private fun snapshotOf(vararg cards: SyncCard) = SyncSnapshot(cards.toList(), emptyList())

    private fun syncCard(
        syncId: String?,
        name: String = "Card",
        content: String = "123",
        changedAt: Long = 0L,
        categoryName: String? = null,
    ) = SyncCard(
        syncId = syncId,
        changedAt = changedAt,
        name = name,
        categoryName = categoryName,
        content = content,
        color = "#FFFFFF",
        format = SupportedFormat.QR_CODE,
        logo = null,
        position = 0,
        comment = null,
    )

    private class Device {
        private val categories = mutableMapOf<String, Long>()
        val cardDao = FakeCardDao { categoryId -> categoryNameOf(categoryId) }
        private val categoryRepository = mockk<CategoryRepository> {
            coEvery { upsertCategoryIfCategoryNameIsNew(any(), any()) } answers {
                categories.getOrPut(secondArg()) { categories.size + 1L }
            }
        }
        val repository = SyncRepository(
            barcodeFileRepository = mockk(relaxed = true),
            cardDao = cardDao,
            categoryRepository = categoryRepository,
        )

        fun categoryNameOf(categoryId: Long?): String? {
            return categories.entries.firstOrNull { it.value == categoryId }?.key
        }

        suspend fun addCard(syncId: String, name: String = "Card", content: String = "123", changedAt: Long = 0L) {
            cardDao.upsert(
                Card(
                    id = Card.NEW_CARD_ID,
                    syncId = syncId,
                    name = name,
                    position = 0,
                    isPinned = false,
                    content = content,
                    color = "#FFFFFF",
                    format = SupportedFormat.QR_CODE,
                    path = null,
                    changedAt = changedAt,
                )
            )
        }

        suspend fun updateCard(syncId: String, name: String, changedAt: Long) {
            val card = cardDao.getCardBySyncId(syncId)!!
            cardDao.upsert(card.copy(name = name, changedAt = changedAt))
        }

        suspend fun deleteCard(syncId: String, deletedAt: Long) {
            cardDao.deleteCardAndRememberDeletion(cardDao.getCardBySyncId(syncId)!!, deletedAt)
        }

        suspend fun sortedSnapshot(): SyncSnapshot {
            val snapshot = repository.getLocalSnapshot()
            return SyncSnapshot(
                cards = snapshot.cards.sortedBy { it.syncId },
                deletedCards = snapshot.deletedCards.sortedBy { it.syncId },
            )
        }
    }

    private class FakeCardDao(
        private val categoryNameOf: (Long?) -> String?,
    ) : CardDao {
        val cards = MutableStateFlow<Map<Long, Card>>(emptyMap())
        private val deletedCards = MutableStateFlow<Map<String, DeletedCard>>(emptyMap())
        private var lastId = 0L

        override fun getCardsAndCategories(): Flow<List<CardAndCategory>> = cards.map { cards ->
            cards.values.sortedBy { it.position }.map { card ->
                val category = categoryNameOf(card.categoryId)?.let { Category(card.categoryId!!, it) }
                CardAndCategory(card, category)
            }
        }

        override fun getCardAndCategory(id: Long): Flow<CardAndCategory?> =
            combine(getCardsAndCategories(), cards) { list, _ -> list.find { it.card.id == id } }

        override fun getCardVersions(): Flow<List<CardVersion>> = cards.map { cards ->
            cards.values.map { CardVersion(it.syncId, it.changedAt) }
        }

        override fun getDeletedCards(): Flow<List<DeletedCard>> = deletedCards.map { it.values.toList() }

        override suspend fun getNumberOfCards(): Int = cards.value.size

        override suspend fun getCard(id: Long): Card? = cards.value[id]

        override suspend fun getCardBySyncId(syncId: String): Card? =
            cards.value.values.find { it.syncId == syncId }

        override suspend fun getCardWithSuchData(name: String, content: String, format: SupportedFormat): Card? =
            getCardsWithSuchData(name, content, format).firstOrNull()

        override suspend fun getCardsWithSuchData(name: String, content: String, format: SupportedFormat): List<Card> =
            cards.value.values.filter { it.name == name && it.content == content && it.format == format }

        override suspend fun getDeletedCard(syncId: String): DeletedCard? = deletedCards.value[syncId]

        override suspend fun getCardsWithNamesLike(name: String): List<Card> = emptyList()

        override suspend fun getCardsWithCategoryIdAndWithNamesLike(categoryId: Long, name: String): List<Card> =
            emptyList()

        override suspend fun deleteCard(id: Long) {
            cards.update { it - id }
        }

        override suspend fun insertDeletedCard(deletedCard: DeletedCard) {
            deletedCards.update { it + (deletedCard.syncId to deletedCard) }
        }

        override suspend fun deleteDeletedCard(syncId: String) {
            deletedCards.update { it - syncId }
        }

        override suspend fun upsert(card: Card): Long {
            val id = if (card.id == Card.NEW_CARD_ID) ++lastId else card.id
            check(cards.value.values.none { it.syncId == card.syncId && it.id != id }) {
                "Unique sync id constraint violated"
            }
            cards.update { it + (id to card.copy(id = id)) }
            return id
        }

        override suspend fun upsert(cards: List<Card>) {
            cards.forEach { upsert(it) }
        }
    }
}
