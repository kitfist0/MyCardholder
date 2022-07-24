package my.cardholder

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Global executor pools for the whole application
 */
@Singleton
class AppExecutors(
    private val analysisExecutor: Executor,
    private val mainExecutor: Executor,
) {

    @Inject
    constructor() : this(
        Executors.newSingleThreadExecutor(),
        MainThreadExecutor()
    )

    fun analysisExecutor(): Executor {
        return analysisExecutor
    }

    fun mainExecutor(): Executor {
        return mainExecutor
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}
