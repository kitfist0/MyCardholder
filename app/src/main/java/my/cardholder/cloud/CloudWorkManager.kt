package my.cardholder.cloud

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import javax.inject.Inject

class CloudWorkManager @Inject constructor(
    private val workManager: WorkManager,
) {

    private companion object {
        const val REMOVAL_WORK_NAME = "removal_work"
        const val UPLOAD_WORK_NAME = "upload_work"
    }

    fun enqueuePeriodicRemovalWork() {
        workManager.enqueueUniquePeriodicWork(
            REMOVAL_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            CloudRemovalWorker.getPeriodicWorkRequest()
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
