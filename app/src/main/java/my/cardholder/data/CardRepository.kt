package my.cardholder.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import my.cardholder.data.model.BarcodeFilePath
import my.cardholder.data.model.Card
import my.cardholder.data.model.CardAndCategory
import my.cardholder.data.model.SupportedFormat
import my.cardholder.data.source.CardDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    private val barcodeFileRepository: BarcodeFileRepository,
    private val cardDao: CardDao,
) {

    private companion object {
        const val DEFAULT_CARD_CONTENT = "12345"
        const val DEFAULT_CARD_NAME = "New"
    }

    val cardsAndCategories: Flow<List<CardAndCategory>> = cardDao.getCardsAndCategories()

    val cardsAndCategoriesForSync = cardDao.getCardsAndCategoriesForSync().distinctUntilChanged()

    fun getCardAndCategory(cardId: Long): Flow<CardAndCategory?> {
        return cardDao.getCardAndCategory(cardId)
    }

    suspend fun getNumberOfPinnedCards(): Int {
        return cardDao.getNumberOfPinnedCards()
    }

    suspend fun deleteCard(cardId: Long) {
        getCard(cardId)?.let { card ->
            card.barcodeFile?.delete()
            cardDao.deleteCard(card.id)
        }
    }

    suspend fun insertNewCard(
        name: String = DEFAULT_CARD_NAME,
        categoryId: Long? = null,
        content: String = DEFAULT_CARD_CONTENT,
        color: String = Card.COLORS.random(),
        format: SupportedFormat = SupportedFormat.QR_CODE,
    ): Long {
        val barcodeFilePath = writeNewBarcodeFile(content, format)
        val newCard = Card(
            id = Card.NEW_CARD_ID,
            name = name,
            isPinned = false,
            categoryId = categoryId,
            content = content,
            color = color,
            format = format,
            path = barcodeFilePath,
            changedAt = System.currentTimeMillis(),
            isSynced = false,
        )
        return upsertCard(newCard)
    }

    suspend fun isCardWithSuchDataExists(
        name: String,
        content: String,
        format: SupportedFormat,
    ): Boolean {
        return cardDao.getCardWithSuchData(name, content, format) != null
    }

    suspend fun updateCardCategoryId(cardId: Long, categoryId: Long?) {
        getCard(cardId)?.let { card ->
            if (card.categoryId != categoryId) {
                upsertCard(
                    card.copy(categoryId = categoryId)
                )
            }
        }
    }

    suspend fun updateCardColor(cardId: Long, color: String) {
        getCard(cardId)?.let { card ->
            if (card.color != color) {
                upsertCard(
                    card.copy(color = color)
                )
            }
        }
    }

    suspend fun updateCardContent(cardId: Long, content: String) {
        getCard(cardId)?.let { card ->
            if (card.content != content) {
                card.barcodeFile?.delete()
                val barcodeFilePath = writeNewBarcodeFile(content, card.format)
                upsertCard(
                    card.copy(content = content, path = barcodeFilePath)
                )
            }
        }
    }

    suspend fun updateCardFormat(cardId: Long, format: SupportedFormat) {
        getCard(cardId)?.let { card ->
            if (card.format != format) {
                card.barcodeFile?.delete()
                val barcodeFilePath = writeNewBarcodeFile(card.content, format)
                upsertCard(
                    card.copy(format = format, path = barcodeFilePath)
                )
            }
        }
    }

    suspend fun updateCardName(cardId: Long, name: String) {
        getCard(cardId)?.let { card ->
            if (card.name != name) {
                upsertCard(
                    card.copy(name = name)
                )
            }
        }
    }

    suspend fun searchCardsBy(name: String, categoryId: Long = -1): List<Card> {
        return if (name.isNotBlank()) {
            if (categoryId.toInt() != -1) {
                cardDao.getCardsWithCategoryIdAndWithNamesLike(categoryId, "%$name%")
            } else {
                cardDao.getCardsWithNamesLike("%$name%")
            }
        } else {
            emptyList()
        }
    }

    suspend fun pinUnpinCard(cardId: Long, isPinned: Boolean) {
        if (isPinned) {
            cardDao.unpinCardWithId(cardId)
        } else {
            cardDao.pinCardWithId(cardId)
        }
    }

    private suspend fun getCard(cardId: Long): Card? {
        return cardDao.getCard(cardId)
    }

    private fun writeNewBarcodeFile(content: String, format: SupportedFormat): BarcodeFilePath? {
        return barcodeFileRepository.writeBarcodeFile(content, format)
    }

    private suspend fun upsertCard(card: Card): Long {
        return cardDao.upsert(
            card.copy(changedAt = System.currentTimeMillis(), isSynced = false)
        )
    }
}
