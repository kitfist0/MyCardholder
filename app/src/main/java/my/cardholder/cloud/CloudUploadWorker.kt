package my.cardholder.cloud

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class CloudUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted private val params: WorkerParameters,
    private val cloudAssistant: CloudAssistant,
) : CoroutineWorker(context, params) {

    companion object {
        private const val REPEAT_INTERVAL_MINUTES = 15
        private const val FLEX_INTERVAL_MINUTES = 10

        fun getPeriodicWorkRequest(vararg fileNameAndContent: FileNameAndContent): PeriodicWorkRequest {
            val keyValueMap = fileNameAndContent.associate { it.getName() to it.getContent() }
            val inputData = Data.Builder()
                .putAll(keyValueMap)
                .build()
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequestBuilder = PeriodicWorkRequest.Builder(
                CloudUploadWorker::class.java,
                REPEAT_INTERVAL_MINUTES.toLong(), TimeUnit.MINUTES,
                FLEX_INTERVAL_MINUTES.toLong(), TimeUnit.MINUTES
            ).apply {
                setConstraints(constraints)
                setInputData(inputData)
            }
            return workRequestBuilder.build()
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
