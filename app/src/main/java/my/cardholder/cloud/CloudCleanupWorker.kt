package my.cardholder.cloud

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CloudCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted private val params: WorkerParameters,
    private val cloudAssistant: CloudAssistant,
) : CoroutineWorker(context, params) {

    companion object {
        private const val FILE_NAMES_KEY = "file_names"

        fun getWorkRequest(fileNames: List<String>): OneTimeWorkRequest {
            val inputData = Data.Builder()
                .putStringArray(FILE_NAMES_KEY, fileNames.toTypedArray())
                .build()
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return OneTimeWorkRequest.Builder(CloudCleanupWorker::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        }
    }

    override suspend fun doWork(): Result {
        val fileNames = params.inputData.getStringArray(FILE_NAMES_KEY)
        return fileNames?.let { names ->
            val uploadResult = cloudAssistant.delete(*names)
            if (uploadResult.isSuccess) {
                Result.success()
            } else {
                Result.failure()
            }
        } ?: Result.success()
    }
}
