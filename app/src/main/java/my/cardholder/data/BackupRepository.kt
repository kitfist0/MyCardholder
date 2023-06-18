package my.cardholder.data

import com.github.doyaaaaaken.kotlincsv.dsl.context.InsufficientFieldsRowBehaviour
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.flow.first
import my.cardholder.data.model.Card
import my.cardholder.data.model.Label
import my.cardholder.data.model.LabelRef
import my.cardholder.data.model.SupportedFormat
import my.cardholder.data.source.CardDao
import my.cardholder.data.source.LabelDao
import my.cardholder.data.source.LabelRefDao
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val barcodeFileRepository: BarcodeFileRepository,
    private val cardDao: CardDao,
    private val labelDao: LabelDao,
    private val labelRefDao: LabelRefDao,
) {

    private companion object {
        const val CSV_SCHEME_VERSION_1 = 1
        const val MIN_NUM_OF_COLUMNS_FOR_VERSION_1 = 4
        const val CARD_NAME_INDEX = 0
        const val CARD_CONTENT_INDEX = 1
        const val CARD_COLOR_INDEX = 2
        const val CARD_BARCODE_FORMAT_INDEX = 3
    }

    suspend fun exportCards(outputStream: OutputStream): Result<Boolean> {
        val cardWithLabels = cardDao.getCardsWithLabels().first()
        return if (cardWithLabels.isNotEmpty()) {
            runCatching {
                csvWriter().openAsync(outputStream) {
                    writeRow(CSV_SCHEME_VERSION_1)
                    cardDao.getCardsWithLabels().first().forEach { cardWithLabels ->
                        val card = cardWithLabels.card
                        val row = mutableListOf<Any>(card.name, card.content, card.color, card.format)
                        val labelTexts = cardWithLabels.labels.map { it.text }
                        row.addAll(labelTexts)
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
                if (version == CSV_SCHEME_VERSION_1) {
                    readAllAsSequence().forEach { row ->
                        importCard(row)
                    }
                }
            }
            true
        }
    }

    private suspend fun importCard(row: List<String>) {
        val name = row[CARD_NAME_INDEX]
        val content = row[CARD_CONTENT_INDEX]
        val format = SupportedFormat.valueOf(row[CARD_BARCODE_FORMAT_INDEX])
        if (cardDao.getCardWithSuchData(name, content, format) == null) {
            val labels = if (row.size > MIN_NUM_OF_COLUMNS_FOR_VERSION_1) {
                row.subList(MIN_NUM_OF_COLUMNS_FOR_VERSION_1, row.lastIndex)
                    .map { Label(0, it) }
            } else {
                emptyList()
            }
            val barcodeFilePath = barcodeFileRepository.writeBarcodeFile(content, format)
            val card = Card(
                id = 0,
                name = row[CARD_NAME_INDEX],
                content = content,
                color = row[CARD_COLOR_INDEX],
                format = format,
                path = barcodeFilePath,
            )
            val cardId = cardDao.insert(card)
            val labelIds = labelDao.upsert(labels)
            val labelRefs = labelIds.map { labelId -> LabelRef(cardId, labelId) }
            labelRefDao.insert(*labelRefs.toTypedArray())
        }
    }
}
