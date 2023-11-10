package my.cardholder.di

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.RuStoreBillingClientFactory
import ru.rustore.sdk.billingclient.provider.logger.ExternalPaymentLogger
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RuStoreModule {

    private const val TAG = "RU_STORE_BILLING"

    @Provides
    @Singleton
    fun provideBillingClient(context: Context): RuStoreBillingClient {
        val logger = object : ExternalPaymentLogger {
            override fun d(e: Throwable?, message: () -> String) {
                Log.d(TAG, message.invoke(), e)
            }

            override fun e(e: Throwable?, message: () -> String) {
                Log.e(TAG, message.invoke(), e)
            }

            override fun i(e: Throwable?, message: () -> String) {
                Log.i(TAG, message.invoke(), e)
            }

            override fun v(e: Throwable?, message: () -> String) {
                Log.v(TAG, message.invoke(), e)
            }

            override fun w(e: Throwable?, message: () -> String) {
                Log.w(TAG, message.invoke(), e)
            }
        }
        return RuStoreBillingClientFactory.create(
            context = context,
            consoleApplicationId = "2063489959",
            deeplinkScheme = "cardholder",
            externalPaymentLoggerFactory = { logger },
            debugLogs = true,
        )
    }
}
