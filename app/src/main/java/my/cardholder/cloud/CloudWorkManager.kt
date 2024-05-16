package my.cardholder.cloud

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import javax.inject.Inject

class CloudWorkManager @Inject constructor(
    private val workManager: WorkManager,
) {

    private companion object {
        const val CLEANUP_WORK_NAME_PREFIX = "cleanup_"
        const val UPLOAD_WORK_NAME_PREFIX = "upload_"
    }

    val uploadWorkInfoList = workManager.getWorkInfosByTagFlow(CloudUploadWorker::class.java.name)

    fun enqueueCleanupWork(uniqueId: Long) {
        workManager.cancelUniqueWork(UPLOAD_WORK_NAME_PREFIX + uniqueId)
        val uniqueWorkName = CLEANUP_WORK_NAME_PREFIX + uniqueId
        val workRequest = CloudCleanupWorker.getWorkRequest(
            fileName = uniqueId.toString(),
        )
        workManager.enqueueUniqueWork(uniqueWorkName, ExistingWorkPolicy.REPLACE, workRequest)
    }

    fun enqueueUploadWork(uniqueId: Long, content: String) {
        val uniqueWorkName = UPLOAD_WORK_NAME_PREFIX + uniqueId
        val workRequest = CloudUploadWorker.getWorkRequest(
            fileName = uniqueId.toString(),
            fileContent = content,
        )
        workManager.enqueueUniqueWork(uniqueWorkName, ExistingWorkPolicy.REPLACE, workRequest)
    }

    fun cancelAllWork() {
        workManager.cancelAllWork()
    }
}
