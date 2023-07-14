package my.cardholder.data

import com.github.doyaaaaaken.kotlincsv.dsl.context.InsufficientFieldsRowBehaviour
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import my.cardholder.data.model.BackupOperationType
import my.cardholder.data.model.BackupResult
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

    fun exportToBackupFile(outputStream: OutputStream): Flow<BackupResult> = channelFlow {
        val cardsAndCategories = cardDao.getCardsAndCategories().first()
        val numOfCards = cardsAndCategories.size
        csvWriter().openAsync(outputStream) {
            writeRow(V1_CSV_SCHEME)
            cardsAndCategories.forEachIndexed { index, cardAndCategory ->
                val card = cardAndCategory.card
                val categoryName = cardAndCategory.category?.name.orEmpty()
                val row = mutableListOf<Any>()
                row.add(V1_CARD_NAME_INDEX, card.name)
                row.add(V1_CARD_CATEGORY_INDEX, categoryName)
                row.add(V1_CARD_CONTENT_INDEX, card.content)
                row.add(V1_CARD_COLOR_INDEX, card.color)
                row.add(V1_CARD_FORMAT_INDEX, card.format)
                writeRow(row)
                sendProgress(
                    backupOperationType = BackupOperationType.EXPORT,
                    current = index + 1,
                    total = numOfCards,
                )
            }
        }
    }.catch {
        emit(BackupResult.Error(it.message.orEmpty()))
    }

    fun importFromBackupFile(inputStream: InputStream): Flow<BackupResult> = channelFlow {
        val reader = csvReader {
            insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.IGNORE
        }
        reader.openAsync(inputStream) {
            val version = readNext()?.first()?.toInt()
            val rows = readAllAsSequence().toList()
            val numOfCards = rows.count()
            if (numOfCards == 0) {
                throw Throwable("Backup file is empty")
            }
            when (version) {
                V1_CSV_SCHEME ->
                    rows.forEachIndexed { index, row ->
                        importCardAccordingToV1Schema(row)
                        sendProgress(
                            backupOperationType = BackupOperationType.IMPORT,
                            current = index + 1,
                            total = numOfCards,
                        )
                    }
                else -> throw Throwable("Invalid file format")
            }
        }
    }.catch {
        emit(BackupResult.Error(it.message.orEmpty()))
    }

    private suspend fun importCardAccordingToV1Schema(row: List<String>) {
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
            cardDao.upsert(card)
        }
    }

    private suspend inline fun ProducerScope<BackupResult>.sendProgress(
        backupOperationType: BackupOperationType,
        current: Int,
        total: Int,
    ) {
        if (current == total) {
            send(BackupResult.Success(backupOperationType))
        } else {
            val percentage = ((current.toDouble() / total) * 100).toInt()
            send(BackupResult.Progress(percentage))
        }
    }
}
