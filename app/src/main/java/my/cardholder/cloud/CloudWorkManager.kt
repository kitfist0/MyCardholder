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

    fun enqueuePeriodicCleanupWork() {
        workManager.enqueueUniquePeriodicWork(
            CLEANUP_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            CloudCleanupWorker.getPeriodicWorkRequest()
        )
    }

    fun enqueuePeriodicUploadWork() {
        workManager.enqueueUniquePeriodicWork(
            UPLOAD_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            CloudUploadWorker.getPeriodicWorkRequest()
        )
    }

    fun cancelAllWork() {
        workManager.cancelAllWork()
    }
}
