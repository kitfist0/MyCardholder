package my.cardholder.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import my.cardholder.cloud.BackupChecksum
import my.cardholder.data.model.Card
import my.cardholder.data.model.CardVersion
import my.cardholder.data.model.DeletedCard
import my.cardholder.data.model.SyncCard
import my.cardholder.data.model.SyncSnapshot
import my.cardholder.data.source.CardDao
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Merges cloud snapshots into the local database.
 *
 * Every card is versioned by its [Card.changedAt]: for each sync id the latest change wins,
 * and a deletion wins over changes made before it. Merging is idempotent and commutative,
 * so devices converge to the same state regardless of the order of syncs.
 */
@Singleton
class SyncRepository @Inject constructor(
    private val barcodeFileRepository: BarcodeFileRepository,
    private val cardDao: CardDao,
    private val categoryRepository: CategoryRepository,
) {

    /** Changes whenever any card is added, changed or deleted */
    val localChecksum: Flow<BackupChecksum> =
        combine(cardDao.getCardVersions(), cardDao.getDeletedCards()) { cardVersions, deletedCards ->
            calculateChecksum(cardVersions, deletedCards)
        }.distinctUntilChanged()

    suspend fun getLocalChecksum(): BackupChecksum = localChecksum.first()

    suspend fun getLocalSnapshot(): SyncSnapshot {
        val cards = cardDao.getCardsAndCategories().first().map { cardAndCategory ->
            val card = cardAndCategory.card
            SyncCard(
                syncId = card.syncId,
                changedAt = card.changedAt,
                name = card.name,
                categoryName = cardAndCategory.category?.name,
                content = card.content,
                color = card.color,
                format = card.format,
                logo = card.logo,
                position = card.position,
                comment = card.comment,
            )
        }
        return SyncSnapshot(cards = cards, deletedCards = cardDao.getDeletedCards().first())
    }

    /** Same state on different devices always gives the same checksum */
    fun calculateChecksum(snapshot: SyncSnapshot): BackupChecksum {
        val cardVersions = snapshot.cards.map { card ->
            CardVersion(syncId = card.syncId.orEmpty(), changedAt = card.changedAt)
        }
        return calculateChecksum(cardVersions, snapshot.deletedCards)
    }

    suspend fun mergeSnapshot(snapshot: SyncSnapshot) {
        val remoteSyncIds = snapshot.cards.mapNotNullTo(mutableSetOf()) { it.syncId } +
                snapshot.deletedCards.map { it.syncId }
        snapshot.deletedCards.forEach { remoteDeletedCard -> mergeDeletedCard(remoteDeletedCard) }
        snapshot.cards.forEach { remoteCard -> mergeCard(remoteCard, remoteSyncIds) }
    }

    private suspend fun mergeDeletedCard(remoteDeletedCard: DeletedCard) {
        val localCard = cardDao.getCardBySyncId(remoteDeletedCard.syncId)
        when {
            localCard == null -> {
                val localDeletedCard = cardDao.getDeletedCard(remoteDeletedCard.syncId)
                if (localDeletedCard == null || localDeletedCard.deletedAt < remoteDeletedCard.deletedAt) {
                    cardDao.insertDeletedCard(remoteDeletedCard)
                }
            }

            localCard.changedAt <= remoteDeletedCard.deletedAt -> {
                localCard.barcodeFile?.delete()
                cardDao.deleteCardAndRememberDeletion(localCard, remoteDeletedCard.deletedAt)
            }

            // Otherwise the card was changed after it was deleted on another device, so it survives
        }
    }

    private suspend fun mergeCard(remoteCard: SyncCard, remoteSyncIds: Set<String>) {
        val syncId = remoteCard.syncId
        val localCard = syncId?.let { cardDao.getCardBySyncId(it) }
        if (localCard != null) {
            if (remoteCard.changedAt > localCard.changedAt) {
                cardDao.upsert(localCard.withRemoteData(remoteCard, syncId))
            }
            return
        }

        if (syncId != null) {
            val localDeletedCard = cardDao.getDeletedCard(syncId)
            if (localDeletedCard != null) {
                if (localDeletedCard.deletedAt >= remoteCard.changedAt) return
                cardDao.deleteDeletedCard(syncId)
            }
        }

        // The same card could be created on several devices independently, e.g. before sync ids existed
        val twinCard = cardDao.getCardsWithSuchData(remoteCard.name, remoteCard.content, remoteCard.format)
            .firstOrNull { card -> card.syncId !in remoteSyncIds }
        if (twinCard != null) {
            if (syncId != null) {
                val mergedCard = if (remoteCard.changedAt > twinCard.changedAt) {
                    twinCard.withRemoteData(remoteCard, syncId)
                } else {
                    twinCard.copy(syncId = syncId)
                }
                cardDao.upsert(mergedCard)
            }
            return
        }

        insertRemoteCard(remoteCard)
    }

    private suspend fun insertRemoteCard(remoteCard: SyncCard) {
        val newCard = Card(
            id = Card.NEW_CARD_ID,
            syncId = remoteCard.syncId ?: UUID.randomUUID().toString(),
            name = remoteCard.name,
            position = remoteCard.position ?: cardDao.getNextPosition(),
            logo = remoteCard.logo,
            isPinned = false,
            categoryId = getCategoryId(remoteCard.categoryName),
            content = remoteCard.content,
            color = remoteCard.color,
            comment = remoteCard.comment,
            format = remoteCard.format,
            path = barcodeFileRepository.writeBarcodeFile(remoteCard.content, remoteCard.format),
            // Cards from old backups have no version, so they become a new local change
            changedAt = if (remoteCard.syncId != null) remoteCard.changedAt else System.currentTimeMillis(),
        )
        cardDao.upsert(newCard)
    }

    /** Keeps the remote version of the card, including its changedAt, so that checksums converge */
    private suspend fun Card.withRemoteData(remoteCard: SyncCard, syncId: String): Card {
        val barcodeChanged = content != remoteCard.content || format != remoteCard.format
        val newPath = if (barcodeChanged || path == null) {
            barcodeFile?.delete()
            barcodeFileRepository.writeBarcodeFile(remoteCard.content, remoteCard.format)
        } else {
            path
        }
        return copy(
            syncId = syncId,
            name = remoteCard.name,
            position = remoteCard.position ?: position,
            logo = remoteCard.logo,
            categoryId = getCategoryId(remoteCard.categoryName),
            content = remoteCard.content,
            color = remoteCard.color,
            comment = remoteCard.comment,
            format = remoteCard.format,
            path = newPath,
            changedAt = remoteCard.changedAt,
        )
    }

    private suspend fun getCategoryId(categoryName: String?): Long? {
        return categoryName?.let { categoryRepository.upsertCategoryIfCategoryNameIsNew(categoryName = it) }
    }

    private fun calculateChecksum(
        cardVersions: List<CardVersion>,
        deletedCards: List<DeletedCard>,
    ): BackupChecksum {
        val digest = MessageDigest.getInstance("SHA-256")
        cardVersions.sortedBy { it.syncId }.forEach { cardVersion ->
            digest.update("c:${cardVersion.syncId}:${cardVersion.changedAt};".toByteArray())
        }
        deletedCards.sortedBy { it.syncId }.forEach { deletedCard ->
            digest.update("d:${deletedCard.syncId}:${deletedCard.deletedAt};".toByteArray())
        }
        // Positive, so that the checksum looks like a plain number in a backup file name
        return ByteBuffer.wrap(digest.digest()).long and Long.MAX_VALUE
    }
}
