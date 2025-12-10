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
import my.cardholder.data.model.SupportedFormat
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val cardRepository: CardRepository,
    private val categoryRepository: CategoryRepository,
) {

    private companion object {
        const val CSV_SCHEME_VERSION = 4
        const val NAME_INDEX_SINCE_V1 = 0
        const val CATEGORY_INDEX_SINCE_V1 = 1
        const val CONTENT_INDEX_SINCE_V1 = 2
        const val COLOR_INDEX_SINCE_V1 = 3
        const val FORMAT_INDEX_SINCE_V1 = 4
        const val LOGO_INDEX_SINCE_V2 = 5
        const val POSITION_INDEX_SINCE_V3 = 6
        const val COMMENT_INDEX_SINCE_V4 = 7
    }

    fun exportToBackupFile(outputStream: OutputStream): Flow<BackupResult> = channelFlow {
        val cardsAndCategories = cardRepository.cardsAndCategories.first().reversed()
        val numOfCards = cardsAndCategories.size
        csvWriter().openAsync(outputStream) {
            writeRow(CSV_SCHEME_VERSION)
            cardsAndCategories.forEachIndexed { index, cardAndCategory ->
                val card = cardAndCategory.card
                val categoryName = cardAndCategory.category?.name.orEmpty()
                val row = mutableListOf<Any>()
                row.add(NAME_INDEX_SINCE_V1, card.name)
                row.add(CATEGORY_INDEX_SINCE_V1, categoryName)
                row.add(CONTENT_INDEX_SINCE_V1, card.content)
                row.add(COLOR_INDEX_SINCE_V1, card.color)
                row.add(FORMAT_INDEX_SINCE_V1, card.format)
                row.add(LOGO_INDEX_SINCE_V2, card.logo.orEmpty())
                row.add(POSITION_INDEX_SINCE_V3, card.position)
                row.add(COMMENT_INDEX_SINCE_V4, card.comment.orEmpty())
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
            val version = readNext()?.first()?.toInt() ?: 0
            val rows = readAllAsSequence().toList().reversed()
            val numOfCards = rows.count()
            if (numOfCards == 0) {
                throw Throwable("Backup file is empty")
            }
            if (version == 0 || version > CSV_SCHEME_VERSION) {
                throw Throwable("Invalid file format")
            }
            rows.forEachIndexed { index, row ->
                importCard(row)
                sendProgress(
                    backupOperationType = BackupOperationType.IMPORT,
                    current = index + 1,
                    total = numOfCards,
                )
            }
        }
    }.catch {
        emit(BackupResult.Error(it.message.orEmpty()))
    }

    private suspend fun importCard(row: List<String>) {
        val name = row[NAME_INDEX_SINCE_V1]
        val content = row[CONTENT_INDEX_SINCE_V1]
        val format = SupportedFormat.valueOf(row[FORMAT_INDEX_SINCE_V1])
        if (!cardRepository.isCardWithSuchDataExists(name, content, format)) {
            val categoryName = row[CATEGORY_INDEX_SINCE_V1]
            val categoryId = if (categoryName.isNotEmpty()) {
                categoryRepository.upsertCategoryIfCategoryNameIsNew(categoryName = categoryName)
            } else {
                null
            }
            cardRepository.insertNewCard(
                name = row[NAME_INDEX_SINCE_V1],
                position = runCatching { row[POSITION_INDEX_SINCE_V3].toInt() }.getOrNull(),
                logo = runCatching { row[LOGO_INDEX_SINCE_V2] }.getOrNull(),
                categoryId = categoryId,
                content = content,
                color = row[COLOR_INDEX_SINCE_V1],
                comment = runCatching { row[COMMENT_INDEX_SINCE_V4] }.getOrNull(),
                format = format,
            )
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
