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
class CloudCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted private val params: WorkerParameters,
    private val cloudAssistant: CloudAssistant,
) : CoroutineWorker(context, params) {

    companion object {
        private const val REPEAT_INTERVAL_MINUTES = 15
        private const val FLEX_INTERVAL_MINUTES = 10
        private const val FILE_NAMES_KEY = "file_names"

        fun getPeriodicWorkRequest(vararg fileName: String): PeriodicWorkRequest {
            val inputData = Data.Builder()
                .putStringArray(FILE_NAMES_KEY, fileName)
                .build()
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequestBuilder = PeriodicWorkRequest.Builder(
                CloudCleanupWorker::class.java,
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
        val fileNames = params.inputData.getStringArray(FILE_NAMES_KEY)
        if (fileNames.isNullOrEmpty()) {
            return Result.success()
        }
        val uploadResult = cloudAssistant.delete(*fileNames)
        return if (uploadResult.isSuccess) {
            Result.success()
        } else {
            Result.failure()
        }
    }
}
