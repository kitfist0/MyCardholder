package my.cardholder.data

import kotlinx.coroutines.flow.Flow
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

    fun getCardAndCategory(cardId: Long): Flow<CardAndCategory?> {
        return cardDao.getCardAndCategory(cardId)
    }

    suspend fun deleteCard(cardId: Long) {
        getCard(cardId)?.let { card ->
            card.barcodeFile?.delete()
            cardDao.deleteCardAndRememberDeletion(card, card.nextChangedAt())
        }
    }

    suspend fun insertNewCard(
        name: String = DEFAULT_CARD_NAME,
        position: Int? = null,
        logo: String? = null,
        categoryId: Long? = null,
        content: String = DEFAULT_CARD_CONTENT,
        color: String? = null,
        comment: String? = null,
        format: SupportedFormat = SupportedFormat.QR_CODE,
    ): Long {
        val barcodeFilePath = writeNewBarcodeFile(content, format)
        val newCard = Card(
            id = Card.NEW_CARD_ID,
            name = name.trim().ifEmpty { DEFAULT_CARD_NAME },
            position = position ?: cardDao.getNextPosition(),
            logo = logo?.trim()?.ifEmpty { null },
            isPinned = false,
            categoryId = categoryId,
            content = content.trim(),
            color = color ?: Card.COLORS.random(),
            comment = comment?.trim()?.ifEmpty { null },
            format = format,
            path = barcodeFilePath,
            changedAt = System.currentTimeMillis(),
        )
        return upsertCard(newCard)
    }

    suspend fun updateCardPositions(rightOrderedCards: List<Card>) {
        val changedCards = rightOrderedCards.mapIndexedNotNull { index, card ->
            if (card.position != index) {
                // Only the position changes, so reordering doesn't overwrite edits made on another device
                card.copy(
                    position = index,
                    positionChangedAt = nextChangeTime(card.positionChangedAt),
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
        cardDao.updateCard(cardId) { card ->
            if (card.categoryId != categoryId) {
                card.copy(
                    categoryId = categoryId,
                    categoryChangedAt = nextChangeTime(card.categoryChangedAt),
                )
            } else {
                null
            }
        }
    }

    suspend fun updateCardColor(cardId: Long, color: String) {
        updateCard(cardId) { card ->
            if (card.color != color) card.copy(color = color) else null
        }
    }

    suspend fun updateCardContent(cardId: Long, content: String) {
        val newContent = content.trim()
        updateCard(cardId) { card ->
            if (card.content != newContent) {
                card.barcodeFile?.delete()
                val barcodeFilePath = writeNewBarcodeFile(newContent, card.format)
                card.copy(content = newContent, path = barcodeFilePath)
            } else {
                null
            }
        }
    }

    suspend fun updateCardComment(cardId: Long, comment: String?) {
        val newComment = comment?.trim()
        updateCard(cardId) { card ->
            if (card.comment != newComment) card.copy(comment = newComment) else null
        }
    }

    suspend fun updateCardFormat(cardId: Long, format: SupportedFormat) {
        updateCard(cardId) { card ->
            if (card.format != format) {
                card.barcodeFile?.delete()
                val barcodeFilePath = writeNewBarcodeFile(card.content, format)
                card.copy(format = format, path = barcodeFilePath)
            } else {
                null
            }
        }
    }

    /** Replaces the card's barcode, e.g. with a newly scanned one. */
    suspend fun updateCardContentAndFormat(cardId: Long, content: String, format: SupportedFormat) {
        val newContent = content.trim()
        updateCard(cardId) { card ->
            if (card.content != newContent || card.format != format) {
                card.barcodeFile?.delete()
                val barcodeFilePath = writeNewBarcodeFile(newContent, format)
                card.copy(content = newContent, format = format, path = barcodeFilePath)
            } else {
                null
            }
        }
    }

    suspend fun updateCardLogo(cardId: Long, logo: String?) {
        val newLogo = logo?.trim()
        updateCard(cardId) { card ->
            if (card.logo != newLogo) card.copy(logo = newLogo) else null
        }
    }

    /** A card can't have an empty name, so an empty one is ignored */
    suspend fun updateCardName(cardId: Long, name: String) {
        val newName = name.trim()
        if (newName.isEmpty()) {
            return
        }
        updateCard(cardId) { card ->
            if (card.name != newName) card.copy(name = newName) else null
        }
    }

    suspend fun searchCardsBy(name: String, categoryId: Long = -1): List<Card> {
        return if (name.isNotBlank()) {
            val namePattern = "%${name.escapeLikeWildcards()}%"
            if (categoryId.toInt() != -1) {
                cardDao.getCardsWithCategoryIdAndWithNamesLike(categoryId, namePattern)
            } else {
                cardDao.getCardsWithNamesLike(namePattern)
            }
        } else {
            emptyList()
        }
    }

    suspend fun getCard(cardId: Long): Card? {
        return cardDao.getCard(cardId)
    }

    /** % and _ typed by the user must match themselves, not any characters */
    private fun String.escapeLikeWildcards(): String {
        return replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
    }

    private fun writeNewBarcodeFile(content: String, format: SupportedFormat): BarcodeFilePath? {
        return barcodeFileRepository.writeBarcodeFile(content, format)
    }

    /** Changes the card atomically (see [CardDao.updateCard]) and marks it as changed for cloud sync */
    private suspend fun updateCard(cardId: Long, change: (Card) -> Card?) {
        cardDao.updateCard(cardId) { card ->
            change(card)?.let { changedCard -> changedCard.copy(changedAt = card.nextChangedAt()) }
        }
    }

    private suspend fun upsertCard(card: Card): Long {
        return cardDao.upsert(
            card.copy(changedAt = card.nextChangedAt())
        )
    }

    /**
     * A change must be newer than the version it replaces, even if that version came from a device
     * whose clock is ahead. Otherwise the change would lose to the old version during cloud sync.
     */
    private fun Card.nextChangedAt(): Long = nextChangeTime(changedAt)

    private fun nextChangeTime(currentChangeTime: Long): Long {
        return maxOf(System.currentTimeMillis(), currentChangeTime + 1)
    }
}
