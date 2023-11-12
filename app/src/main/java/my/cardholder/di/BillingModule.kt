package my.cardholder.di

import android.content.Context
import com.android.billingclient.api.BillingClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import my.cardholder.util.billing.GooglePlayBillingAssistant
import my.cardholder.util.billing.BillingAssistant
import my.cardholder.util.billing.GooglePlayBillingWrapper
import my.cardholder.util.billing.RuStoreBillingAssistant
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.RuStoreBillingClientFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class GooglePlayBilling

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class RuStoreBilling

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {
    @Provides
    @Singleton
    fun provideGooglePlayBillingWrapper(context: Context): GooglePlayBillingWrapper {
        return GooglePlayBillingWrapper(BillingClient.newBuilder(context))
    }

    @Provides
    @Singleton
    @GooglePlayBilling
    fun provideGooglePlayBillingAssistant(googlePlayBillingWrapper: GooglePlayBillingWrapper): BillingAssistant {
        return GooglePlayBillingAssistant(googlePlayBillingWrapper)
    }

    @Provides
    @Singleton
    fun provideRuStoreBillingClient(context: Context): RuStoreBillingClient {
        return RuStoreBillingClientFactory.create(
            context = context,
            consoleApplicationId = "2063489959",
            deeplinkScheme = "cardholder",
        )
    }

    @Provides
    @Singleton
    @RuStoreBilling
    fun provideRuStoreBillingAssistant(ruStoreBillingClient: RuStoreBillingClient): BillingAssistant {
        return RuStoreBillingAssistant(ruStoreBillingClient)
    }
}
