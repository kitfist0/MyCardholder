package my.cardholder.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import my.cardholder.data.model.BarcodeFilePath
import my.cardholder.data.model.Card
import my.cardholder.data.model.CardAndCategory
import my.cardholder.data.model.SupportedFormat
import my.cardholder.data.source.CardDao
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    private val barcodeFileRepository: BarcodeFileRepository,
    private val cardDao: CardDao,
) {

    private companion object {
        const val CARD_NAME_FORMAT = "MMMdd HH:mm:ss"
    }

    val cards: Flow<List<Card>> = cardDao.getCards()

    val cardsAndCategories: Flow<List<CardAndCategory>> = cardDao.getCardsAndCategories()

    fun getCardAndCategory(cardId: Long): Flow<CardAndCategory?> {
        return cardDao.getCardAndCategory(cardId)
    }

    suspend fun insertRandomCard(): Long {
        return insertCard(
            content = "Sample text",
            supportedFormat = SupportedFormat.QR_CODE,
        )
    }

    suspend fun insertCard(content: String, supportedFormat: SupportedFormat): Long {
        val timestamp = System.currentTimeMillis()
        val barcodeFilePath = writeNewBarcodeFile(content, supportedFormat)
        val newCard = Card(
            id = Card.NEW_CARD_ID,
            name = SimpleDateFormat(CARD_NAME_FORMAT, Locale.US).format(timestamp),
            content = content,
            color = Card.COLORS.random(),
            format = supportedFormat,
            path = barcodeFilePath,
        )
        return cardDao.upsert(newCard)
    }

    suspend fun searchForCardsWithNamesLike(name: String): List<Card> {
        return if (name.isNotBlank()) {
            cardDao.getCardsWithNamesLike("%$name%")
        } else {
            emptyList()
        }
    }

    suspend fun updateCardName(cardId: Long, name: String) {
        getCard(cardId)?.copy(name = name)
            ?.let { card -> cardDao.upsert(card) }
    }

    suspend fun updateCardCategoryId(cardId: Long, categoryId: Long?) {
        getCard(cardId)?.copy(categoryId = categoryId)
            ?.let { card -> cardDao.upsert(card) }
    }

    suspend fun updateCardColor(cardId: Long, color: String) {
        getCard(cardId)?.copy(color = color)
            ?.let { card -> cardDao.upsert(card) }
    }

    suspend fun updateCardBarcodeIfRequired(
        cardId: Long,
        content: String?,
        format: SupportedFormat?,
    ) {
        val oldCard = getCard(cardId) ?: return
        val oldCardContent = oldCard.content
        val oldCardFormat = oldCard.format
        if (oldCardContent == content && oldCardFormat == format) {
            return
        }
        oldCard.barcodeFile?.delete()
        val cardContent = content ?: oldCardContent
        val barcodeFormat = format ?: oldCardFormat
        val barcodeFilePath = writeNewBarcodeFile(cardContent, barcodeFormat)
        val card = oldCard.copy(
            content = cardContent,
            format = barcodeFormat,
            path = barcodeFilePath,
        )
        cardDao.upsert(card)
    }

    suspend fun deleteCard(cardId: Long) {
        getCard(cardId)?.let { card ->
            card.barcodeFile?.delete()
            cardDao.deleteCard(card.id)
        }
    }

    private suspend fun getCard(cardId: Long): Card? {
        return cardDao.getCard(cardId).first()
    }

    private fun writeNewBarcodeFile(content: String, format: SupportedFormat): BarcodeFilePath? {
        return barcodeFileRepository.writeBarcodeFile(content, format)
    }
}
