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
        private const val FILE_NAME_KEY = "file_name"

        fun getWorkRequest(fileName: String): OneTimeWorkRequest {
            val inputData = Data.Builder()
                .putString(FILE_NAME_KEY, fileName)
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
        val fileName = params.inputData.getString(FILE_NAME_KEY)
        return fileName?.let { name ->
            val uploadResult = cloudAssistant.delete(name)
            if (uploadResult.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } ?: Result.failure()
    }
}
