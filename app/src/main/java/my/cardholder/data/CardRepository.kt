package my.cardholder.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
        val card = createNewCardAndBarcodeFile(
            name = SimpleDateFormat(CARD_NAME_FORMAT, Locale.US).format(timestamp),
            content = content,
            format = supportedFormat,
        )
        return cardDao.insert(card)
    }

    suspend fun searchForCardsWithNamesLike(name: String): List<Card> {
        return if (name.isNotBlank()) {
            cardDao.getCardsWithNamesLike("%$name%")
        } else {
            emptyList()
        }
    }

    suspend fun updateCardCategoryId(cardId: Long, categoryId: Long?) {
        val oldCard = getCard(cardId).first()
        oldCard?.copy(categoryId = categoryId)
            ?.let { newCard -> cardDao.update(newCard) }
    }

    suspend fun updateCardColor(cardId: Long, color: String) {
        val oldCard = getCard(cardId).first()
        oldCard?.copy(color = color)
            ?.let { newCard -> cardDao.update(newCard) }
    }

    suspend fun updateCardDataIfItChanges(
        id: Long,
        name: String?,
        content: String?,
        format: SupportedFormat?,
    ) {
        val oldCard = getCard(id).first() ?: return
        val oldCardName = oldCard.name
        val oldCardContent = oldCard.content
        val oldCardFormat = oldCard.format
        if (oldCardName == name && oldCardContent == content && oldCardFormat == format) {
            return
        }
        val newCard = if (oldCardContent != content || oldCardFormat != format) {
            oldCard.barcodeFile?.delete()
            createNewCardAndBarcodeFile(
                id = oldCard.id,
                name = name ?: oldCardName,
                content = content ?: oldCardContent,
                color = oldCard.color,
                format = format ?: oldCardFormat,
            )
        } else {
            oldCard.copy(
                name = name ?: oldCardName,
                content = content,
            )
        }
        cardDao.update(newCard)
    }

    suspend fun deleteCard(cardId: Long) {
        getCard(cardId).first()?.let { card ->
            card.barcodeFile?.delete()
            cardDao.deleteCard(card.id)
        }
    }

    private fun getCard(cardId: Long): Flow<Card?> {
        return cardDao.getCard(cardId)
    }

    private fun createNewCardAndBarcodeFile(
        id: Long? = null,
        name: String,
        content: String,
        color: String? = null,
        format: SupportedFormat,
    ): Card {
        val barcodeFilePath = barcodeFileRepository.writeBarcodeFile(content, format)
        return Card(
            id = id ?: 0,
            name = name,
            content = content,
            color = color ?: Card.COLORS.random(),
            format = format,
            path = barcodeFilePath,
        )
    }
}
