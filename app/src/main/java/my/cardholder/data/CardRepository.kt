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

    val checksumOfAllCards: Flow<Long> = cardDao.getChecksumOfAllCards().distinctUntilChanged()

    fun getCardAndCategory(cardId: Long): Flow<CardAndCategory?> {
        return cardDao.getCardAndCategory(cardId)
    }

    suspend fun deleteCard(cardId: Long) {
        getCard(cardId)?.let { card ->
            card.barcodeFile?.delete()
            cardDao.deleteCard(card.id)
        }
    }

    suspend fun insertNewCard(
        name: String = DEFAULT_CARD_NAME,
        position: Int? = null,
        logo: String? = null,
        categoryId: Long? = null,
        content: String = DEFAULT_CARD_CONTENT,
        color: String = Card.COLORS.random(),
        comment: String? = null,
        format: SupportedFormat = SupportedFormat.QR_CODE,
    ): Long {
        val barcodeFilePath = writeNewBarcodeFile(content, format)
        val newCard = Card(
            id = Card.NEW_CARD_ID,
            name = name.trim(),
            position = position ?: cardDao.getNumberOfCards(),
            logo = logo?.trim()?.ifEmpty { null },
            isPinned = false,
            categoryId = categoryId,
            content = content.trim(),
            color = color,
            comment = comment?.trim()?.ifEmpty { null },
            format = format,
            path = barcodeFilePath,
            changedAt = System.currentTimeMillis(),
        )
        return upsertCard(newCard)
    }

    suspend fun updateCardPositions(rightOrderedCards: List<Card>) {
        val timestamp = System.currentTimeMillis()
        val changedCards = rightOrderedCards.mapIndexedNotNull { index, card ->
            if (card.position != index) {
                card.copy(
                    position = index,
                    changedAt = timestamp,
                )
            } else {
                null
            }
        }
        if (changedCards.isNotEmpty()) {
            cardDao.upsert(changedCards)
        }
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
            val newContent = content.trim()
            if (card.content != newContent) {
                card.barcodeFile?.delete()
                val barcodeFilePath = writeNewBarcodeFile(newContent, card.format)
                upsertCard(
                    card.copy(content = newContent, path = barcodeFilePath)
                )
            }
        }
    }

    suspend fun updateCardComment(cardId: Long, comment: String?) {
        getCard(cardId)?.let { card ->
            val newComment = comment?.trim()
            if (card.comment != newComment) {
                upsertCard(
                    card.copy(comment = newComment)
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

    // suspend fun updateCardLogo(cardId: Long, logo: String?) {
    //     val newLogo = logo?.trim()
    //     getCard(cardId)?.let { card ->
    //         if (card.logo != newLogo) {
    //             upsertCard(
    //                 card.copy(logo = newLogo)
    //             )
    //         }
    //     }
    // }

    suspend fun updateCardName(cardId: Long, name: String) {
        val newName = name.trim()
        getCard(cardId)?.let { card ->
            if (card.name != newName) {
                upsertCard(
                    card.copy(name = newName)
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

    private suspend fun getCard(cardId: Long): Card? {
        return cardDao.getCard(cardId)
    }

    private fun writeNewBarcodeFile(content: String, format: SupportedFormat): BarcodeFilePath? {
        return barcodeFileRepository.writeBarcodeFile(content, format)
    }

    private suspend fun upsertCard(card: Card): Long {
        return cardDao.upsert(
            card.copy(changedAt = System.currentTimeMillis())
        )
    }
}
