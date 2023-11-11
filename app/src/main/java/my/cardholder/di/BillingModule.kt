package my.cardholder.di

import android.content.Context
import com.android.billingclient.api.BillingClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import my.cardholder.util.billing.GooglePlayBillingAssistant
import my.cardholder.util.billing.BillingAssistant
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {
    @Provides
    @Singleton
    fun provideBillingAssistant(context: Context): BillingAssistant {
        return GooglePlayBillingAssistant(BillingClient.newBuilder(context))
    }
}
