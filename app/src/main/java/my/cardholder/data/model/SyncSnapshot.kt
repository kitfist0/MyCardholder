package my.cardholder.data.model

/** State of all cards that is exchanged with the cloud */
data class SyncSnapshot(
    val cards: List<SyncCard>,
    val deletedCards: List<DeletedCard>,
)

data class SyncCard(
    /** Null for cards from backups created before sync ids were introduced */
    val syncId: String?,
    val changedAt: Long,
    val name: String,
    val categoryName: String?,
    val content: String,
    val color: String,
    val format: SupportedFormat,
    val logo: String?,
    val position: Int?,
    val comment: String?,
)
