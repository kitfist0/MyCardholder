package my.cardholder.cloud

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import javax.inject.Inject

class CloudWorkManager @Inject constructor(
    private val workManager: WorkManager,
) {

    private companion object {
        const val CLEANUP_WORK_NAME = "cleanup_work"
        const val UPLOAD_WORK_NAME = "upload_work"
    }

    fun enqueueCleanupWork(vararg fileName: String) {
        val workRequest = CloudCleanupWorker.getPeriodicWorkRequest(*fileName)
        workManager.enqueueUniquePeriodicWork(
            CLEANUP_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun enqueueUploadWork(vararg fileNameAndContent: FileNameAndContent) {
        val workRequest = CloudUploadWorker.getPeriodicWorkRequest(*fileNameAndContent)
        workManager.enqueueUniquePeriodicWork(
            UPLOAD_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cancelAllWork() {
        workManager.cancelAllWork()
    }
}
