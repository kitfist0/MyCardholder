package my.cardholder.data.model

enum class BackupOperationType {
    IMPORT,
    EXPORT,
}

enum class BackupError {
    /** The file is not a card backup at all, e.g. an image or a CSV file of another app */
    NOT_BACKUP_FILE,

    /** The file was created by a newer version of the app */
    NEWER_BACKUP_VERSION,

    /** The file is a card backup, but it has no cards */
    NO_CARDS,

    /** The file is a card backup, but none of its cards can be read */
    DAMAGED_BACKUP_FILE,

    /** Reading or writing failed for another reason, e.g. the file became unavailable */
    OPERATION_FAILED,
}

sealed class BackupResult {
    data class Error(val error: BackupError, val details: String? = null) : BackupResult()
    data class Progress(val percentage: Int) : BackupResult()
    data class Success(val type: BackupOperationType, val numOfSkippedCards: Int = 0) : BackupResult()
}
