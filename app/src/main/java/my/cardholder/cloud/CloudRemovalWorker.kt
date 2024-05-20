package my.cardholder.cloud

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class CloudRemovalWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted private val params: WorkerParameters,
    private val cloudAssistant: CloudAssistant,
    private val cloudTaskStore: CloudTaskStore,
) : CoroutineWorker(context, params) {

    companion object {
        private const val REPEAT_INTERVAL_MINUTES = 15
        private const val FLEX_INTERVAL_MINUTES = 10

        fun getPeriodicWorkRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequestBuilder = PeriodicWorkRequest.Builder(
                CloudRemovalWorker::class.java,
                REPEAT_INTERVAL_MINUTES.toLong(), TimeUnit.MINUTES,
                FLEX_INTERVAL_MINUTES.toLong(), TimeUnit.MINUTES
            )
            workRequestBuilder.setConstraints(constraints)
            return workRequestBuilder.build()
        }
    }

    override suspend fun doWork(): Result {
        cloudTaskStore.removalTasks.first().onEach { task ->
            val fileName = task.fileName
            cloudAssistant.delete(fileName)
                .onSuccess { cloudTaskStore.removeRemovalTaskBy(fileName) }
        }
        return Result.success()
    }
}
