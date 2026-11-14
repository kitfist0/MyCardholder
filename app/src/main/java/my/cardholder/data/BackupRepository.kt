package my.cardholder.data

import com.github.doyaaaaaken.kotlincsv.dsl.context.ExcessFieldsRowBehaviour
import com.github.doyaaaaaken.kotlincsv.dsl.context.InsufficientFieldsRowBehaviour
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import my.cardholder.data.model.BackupError
import my.cardholder.data.model.BackupOperationType
import my.cardholder.data.model.BackupResult
import my.cardholder.data.model.DeletedCard
import my.cardholder.data.model.SupportedFormat
import my.cardholder.data.model.SyncCard
import my.cardholder.data.model.SyncSnapshot
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val cardRepository: CardRepository,
    private val categoryRepository: CategoryRepository,
) {

    companion object {
        const val CSV_SCHEME_VERSION = 5
        private const val NAME_INDEX_SINCE_V1 = 0
        private const val CATEGORY_INDEX_SINCE_V1 = 1
        private const val CONTENT_INDEX_SINCE_V1 = 2
        private const val COLOR_INDEX_SINCE_V1 = 3
        private const val FORMAT_INDEX_SINCE_V1 = 4
        private const val LOGO_INDEX_SINCE_V2 = 5
        private const val POSITION_INDEX_SINCE_V3 = 6
        private const val COMMENT_INDEX_SINCE_V4 = 7
        private const val SYNC_ID_INDEX_SINCE_V5 = 8
        private const val CHANGED_AT_INDEX_SINCE_V5 = 9
        private const val IS_DELETED_INDEX_SINCE_V5 = 10
        private const val NUM_OF_COLUMNS_SINCE_V5 = 11
        private const val TRUE = "1"
        private const val FALSE = "0"
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
                row.add(SYNC_ID_INDEX_SINCE_V5, card.syncId)
                row.add(CHANGED_AT_INDEX_SINCE_V5, card.changedAt)
                row.add(IS_DELETED_INDEX_SINCE_V5, FALSE)
                writeRow(row)
                sendProgress(
                    backupOperationType = BackupOperationType.EXPORT,
                    current = index + 1,
                    total = numOfCards,
                )
            }
        }
    }.catch {
        emit(BackupResult.Error(BackupError.OPERATION_FAILED, it.message))
    }

    /**
     * Imports cards that are not in the database yet. Damaged rows are skipped and counted,
     * so a single bad row doesn't stop the import of the other cards.
     */
    fun importFromBackupFile(inputStream: InputStream): Flow<BackupResult> = channelFlow {
        val reader = csvReader {
            // Rows of older scheme versions have fewer columns, their missing values are empty
            insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.EMPTY_STRING
            excessFieldsRowBehaviour = ExcessFieldsRowBehaviour.TRIM
        }
        reader.openAsync(inputStream) {
            val version = runCatching { readNext()?.firstOrNull()?.trim()?.toIntOrNull() }
                .getOrNull()
                ?.takeIf { it > 0 }
                ?: throw BackupException(BackupError.NOT_BACKUP_FILE)
            if (version > CSV_SCHEME_VERSION) {
                throw BackupException(BackupError.NEWER_BACKUP_VERSION)
            }
            val rows = runCatching { readAllAsSequence().toList() }
                .getOrElse { throw BackupException(BackupError.DAMAGED_BACKUP_FILE) }
                .reversed()
                .filterNot { row -> row.isDeletedCardRow(version) }
            if (rows.isEmpty()) {
                throw BackupException(BackupError.NO_CARDS)
            }
            val backupCards = rows.mapNotNull { row -> row.toBackupCardOrNull() }
            val numOfSkippedCards = rows.size - backupCards.size
            if (backupCards.isEmpty()) {
                throw BackupException(BackupError.DAMAGED_BACKUP_FILE)
            }
            // Cards are added after the existing ones in the order they had, positions from the file
            // would mix them up with the existing cards. Cards of old backups have no positions.
            backupCards.sortedBy { it.position ?: Int.MAX_VALUE }.forEachIndexed { index, backupCard ->
                importCard(backupCard)
                sendProgress(
                    backupOperationType = BackupOperationType.IMPORT,
                    current = index + 1,
                    total = backupCards.size,
                    numOfSkippedCards = numOfSkippedCards,
                )
            }
        }
    }.catch {
        emit(
            if (it is BackupException) {
                BackupResult.Error(it.error)
            } else {
                BackupResult.Error(BackupError.OPERATION_FAILED, it.message)
            }
        )
    }

    /** Serializes the snapshot, including deleted cards, for uploading to the cloud */
    suspend fun encodeSyncSnapshot(snapshot: SyncSnapshot): String {
        val outputStream = ByteArrayOutputStream()
        csvWriter().openAsync(outputStream) {
            writeRow(CSV_SCHEME_VERSION)
            snapshot.cards.forEach { card -> writeRow(card.toRow()) }
            snapshot.deletedCards.forEach { deletedCard -> writeRow(deletedCard.toRow()) }
        }
        return outputStream.toString(Charsets.UTF_8.name())
    }

    /** Parses a cloud backup of any supported scheme version */
    suspend fun decodeSyncSnapshot(content: String): SyncSnapshot {
        val reader = csvReader {
            insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.IGNORE
        }
        return reader.openAsync(content.byteInputStream()) {
            val version = readNext()?.firstOrNull()?.toIntOrNull() ?: 0
            if (version == 0 || version > CSV_SCHEME_VERSION) {
                throw Throwable("Invalid file format")
            }
            val cards = mutableListOf<SyncCard>()
            val deletedCards = mutableListOf<DeletedCard>()
            readAllAsSequence().forEach { row ->
                if (row.isDeletedCardRow(version)) {
                    deletedCards.add(
                        DeletedCard(
                            syncId = row[SYNC_ID_INDEX_SINCE_V5],
                            deletedAt = row[CHANGED_AT_INDEX_SINCE_V5].toLong(),
                        )
                    )
                } else {
                    cards.add(row.toSyncCard(version))
                }
            }
            SyncSnapshot(cards = cards, deletedCards = deletedCards)
        }
    }

    private fun List<String>.isDeletedCardRow(version: Int): Boolean {
        return version >= 5 && getOrNull(IS_DELETED_INDEX_SINCE_V5) == TRUE
    }

    private fun List<String>.toSyncCard(version: Int): SyncCard {
        val hasSyncData = version >= 5
        return SyncCard(
            syncId = if (hasSyncData) this[SYNC_ID_INDEX_SINCE_V5].ifEmpty { null } else null,
            changedAt = if (hasSyncData) this[CHANGED_AT_INDEX_SINCE_V5].toLong() else 0L,
            name = this[NAME_INDEX_SINCE_V1],
            categoryName = this[CATEGORY_INDEX_SINCE_V1].ifEmpty { null },
            content = this[CONTENT_INDEX_SINCE_V1],
            color = this[COLOR_INDEX_SINCE_V1],
            format = SupportedFormat.valueOf(this[FORMAT_INDEX_SINCE_V1]),
            logo = getOrNull(LOGO_INDEX_SINCE_V2)?.ifEmpty { null },
            position = getOrNull(POSITION_INDEX_SINCE_V3)?.toIntOrNull(),
            comment = getOrNull(COMMENT_INDEX_SINCE_V4)?.ifEmpty { null },
        )
    }

    private fun SyncCard.toRow(): List<Any> {
        val row = MutableList<Any>(NUM_OF_COLUMNS_SINCE_V5) { "" }
        row[NAME_INDEX_SINCE_V1] = name
        row[CATEGORY_INDEX_SINCE_V1] = categoryName.orEmpty()
        row[CONTENT_INDEX_SINCE_V1] = content
        row[COLOR_INDEX_SINCE_V1] = color
        row[FORMAT_INDEX_SINCE_V1] = format
        row[LOGO_INDEX_SINCE_V2] = logo.orEmpty()
        row[POSITION_INDEX_SINCE_V3] = position?.toString().orEmpty()
        row[COMMENT_INDEX_SINCE_V4] = comment.orEmpty()
        row[SYNC_ID_INDEX_SINCE_V5] = syncId.orEmpty()
        row[CHANGED_AT_INDEX_SINCE_V5] = changedAt
        row[IS_DELETED_INDEX_SINCE_V5] = FALSE
        return row
    }

    /** Deleted cards keep the column count of regular rows, otherwise the reader skips them */
    private fun DeletedCard.toRow(): List<Any> {
        val row = MutableList<Any>(NUM_OF_COLUMNS_SINCE_V5) { "" }
        row[SYNC_ID_INDEX_SINCE_V5] = syncId
        row[CHANGED_AT_INDEX_SINCE_V5] = deletedAt
        row[IS_DELETED_INDEX_SINCE_V5] = TRUE
        return row
    }

    /** A card row of a backup file, null if the row is damaged */
    private fun List<String>.toBackupCardOrNull(): BackupCard? {
        val format = getOrNull(FORMAT_INDEX_SINCE_V1)?.let { name ->
            SupportedFormat.entries.firstOrNull { it.name == name }
        }
        val content = getOrNull(CONTENT_INDEX_SINCE_V1)
        if (format == null || content.isNullOrBlank()) {
            return null
        }
        return BackupCard(
            name = this[NAME_INDEX_SINCE_V1],
            categoryName = getOrNull(CATEGORY_INDEX_SINCE_V1).orEmpty(),
            content = content,
            color = getOrNull(COLOR_INDEX_SINCE_V1).orEmpty(),
            format = format,
            logo = getOrNull(LOGO_INDEX_SINCE_V2),
            position = getOrNull(POSITION_INDEX_SINCE_V3)?.toIntOrNull(),
            comment = getOrNull(COMMENT_INDEX_SINCE_V4),
        )
    }

    private suspend fun importCard(backupCard: BackupCard) {
        with(backupCard) {
            if (cardRepository.isCardWithSuchDataExists(name, content, format)) {
                return
            }
            val categoryId = if (categoryName.isNotEmpty()) {
                categoryRepository.upsertCategoryIfCategoryNameIsNew(categoryName = categoryName)
            } else {
                null
            }
            cardRepository.insertNewCard(
                name = name,
                logo = logo,
                categoryId = categoryId,
                content = content,
                color = color.ifEmpty { null },
                comment = comment,
                format = format,
            )
        }
    }

    private suspend inline fun ProducerScope<BackupResult>.sendProgress(
        backupOperationType: BackupOperationType,
        current: Int,
        total: Int,
        numOfSkippedCards: Int = 0,
    ) {
        if (current == total) {
            send(BackupResult.Success(backupOperationType, numOfSkippedCards))
        } else {
            val percentage = ((current.toDouble() / total) * 100).toInt()
            send(BackupResult.Progress(percentage))
        }
    }
}

private class BackupException(val error: BackupError) : Exception()

private class BackupCard(
    val name: String,
    val categoryName: String,
    val content: String,
    val color: String,
    val format: SupportedFormat,
    val logo: String?,
    val position: Int?,
    val comment: String?,
)
