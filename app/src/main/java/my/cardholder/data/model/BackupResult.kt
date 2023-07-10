package my.cardholder.data.model

sealed class BackupResult {
    data class Error(val message: String) : BackupResult()
    data class Progress(val percentage: Int) : BackupResult()
    object Success : BackupResult()
}
