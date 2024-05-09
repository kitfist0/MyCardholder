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
        fun getWorkRequest(list: List<FileNameAndContent>): OneTimeWorkRequest {
            val keyValueMap = list.associate { it.getName() to it.getContent() }
            val inputData = Data.Builder()
                .putAll(keyValueMap)
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
        val keyValueMap = params.inputData.keyValueMap
        if (keyValueMap.isEmpty()) {
            return Result.success()
        }
        val fileNameAndContent = keyValueMap.entries
            .map { it.key to it.value.toString() }
            .toTypedArray()
        val uploadResult = cloudAssistant.upload(*fileNameAndContent)
        return if (uploadResult.isSuccess) {
            Result.success()
        } else {
            Result.failure()
        }
    }
}
