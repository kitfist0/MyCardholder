package my.cardholder.data

import com.github.doyaaaaaken.kotlincsv.client.CsvFileReader
import com.github.doyaaaaaken.kotlincsv.dsl.context.InsufficientFieldsRowBehaviour
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.flow.first
import my.cardholder.data.model.Card
import my.cardholder.data.model.Category
import my.cardholder.data.model.SupportedFormat
import my.cardholder.data.source.CardDao
import my.cardholder.data.source.CategoryDao
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val barcodeFileRepository: BarcodeFileRepository,
    private val cardDao: CardDao,
    private val categoryDao: CategoryDao,
) {

    private companion object {
        const val V1_CSV_SCHEME = 1
        const val V1_CARD_NAME_INDEX = 0
        const val V1_CARD_CATEGORY_INDEX = 1
        const val V1_CARD_CONTENT_INDEX = 2
        const val V1_CARD_COLOR_INDEX = 3
        const val V1_CARD_FORMAT_INDEX = 4
    }

    suspend fun exportCards(outputStream: OutputStream): Result<Boolean> {
        val cardsAndCategories = cardDao.getCardsAndCategories().first()
        return if (cardsAndCategories.isNotEmpty()) {
            runCatching {
                csvWriter().openAsync(outputStream) {
                    writeRow(V1_CSV_SCHEME)
                    cardsAndCategories.forEach { cardAndCategory ->
                        val card = cardAndCategory.card
                        val categoryName = cardAndCategory.category?.name.orEmpty()
                        val row = mutableListOf<Any>()
                        row.add(V1_CARD_NAME_INDEX, card.name)
                        row.add(V1_CARD_CATEGORY_INDEX, categoryName)
                        row.add(V1_CARD_CONTENT_INDEX, card.content)
                        row.add(V1_CARD_COLOR_INDEX, card.color)
                        row.add(V1_CARD_FORMAT_INDEX, card.format)
                        writeRow(row)
                    }
                }
                true
            }
        } else {
            Result.failure(Throwable("No cards to export"))
        }
    }

    suspend fun importCards(inputStream: InputStream): Result<Boolean> {
        val reader = csvReader {
            insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.IGNORE
        }
        return runCatching {
            reader.openAsync(inputStream) {
                val version = readNext()?.first()?.toInt()
                if (version == V1_CSV_SCHEME) {
                    importCardsAccordingToV1Schema()
                }
            }
            true
        }
    }

    private suspend fun CsvFileReader.importCardsAccordingToV1Schema() {
        readAllAsSequence().forEach { row ->
            val name = row[V1_CARD_NAME_INDEX]
            val content = row[V1_CARD_CONTENT_INDEX]
            val format = SupportedFormat.valueOf(row[V1_CARD_FORMAT_INDEX])
            if (cardDao.getCardWithSuchData(name, content, format) == null) {
                val categoryName = row[V1_CARD_CATEGORY_INDEX]
                val categoryId = if (categoryName.isNotEmpty()) {
                    categoryDao.getCategoryByName(categoryName)?.id
                        ?: categoryDao.upsert(Category(name = categoryName))
                } else {
                    null
                }
                val barcodeFilePath = barcodeFileRepository.writeBarcodeFile(content, format)
                val card = Card(
                    id = Card.NEW_CARD_ID,
                    name = row[V1_CARD_NAME_INDEX],
                    categoryId = categoryId,
                    content = content,
                    color = row[V1_CARD_COLOR_INDEX],
                    format = format,
                    path = barcodeFilePath,
                )
                cardDao.insert(card)
            }
        }
    }
}
