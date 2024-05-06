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
class CloudUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted private val params: WorkerParameters,
    private val cloudAssistant: CloudAssistant,
) : CoroutineWorker(context, params) {

    companion object {
        private const val FILE_NAME = "file_name"
        private const val FILE_CONTENT = "file_content"

        fun getWorkRequest(fileName: String, fileContent: String): OneTimeWorkRequest {
            val inputData = Data.Builder()
                .putString(FILE_NAME, fileName)
                .putString(FILE_CONTENT, fileContent)
                .build()
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return OneTimeWorkRequest.Builder(CloudUploadWorker::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        }
    }

    override suspend fun doWork(): Result {
        val fileName = params.inputData.getString(FILE_NAME)
        val fileContent = params.inputData.getString(FILE_CONTENT)
        if (fileName.isNullOrEmpty() || fileContent.isNullOrEmpty()) {
            return Result.failure()
        }
        val uploadResult = cloudAssistant.upload(fileName to fileContent)
        return if (uploadResult.isSuccess) {
            Result.success()
        } else {
            Result.failure()
        }
    }
}
