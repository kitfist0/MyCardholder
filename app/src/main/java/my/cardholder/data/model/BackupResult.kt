package my.cardholder.data.model

enum class BackupOperationType {
    IMPORT,
    EXPORT,
}

sealed class BackupResult {
    data class Error(val message: String) : BackupResult()
    data class Progress(val percentage: Int) : BackupResult()
    data class Success(val type: BackupOperationType) : BackupResult()
}
