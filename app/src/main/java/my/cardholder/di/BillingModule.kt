package my.cardholder.di

import android.content.Context
import com.android.billingclient.api.BillingClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import my.cardholder.util.billing.GooglePlayBillingAssistant
import my.cardholder.util.billing.RuStoreBillingAssistant
import ru.rustore.sdk.billingclient.RuStoreBillingClientFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {
    @Provides
    @Singleton
    fun provideGooglePlayBillingAssistant(
        context: Context
    ): GooglePlayBillingAssistant {
        return GooglePlayBillingAssistant(BillingClient.newBuilder(context))
    }

    @Provides
    @Singleton
    fun provideRuStoreBillingAssistant(
        context: Context
    ): RuStoreBillingAssistant {
        val ruStoreBillingClient = RuStoreBillingClientFactory.create(
            context = context,
            consoleApplicationId = "2063489959",
            deeplinkScheme = "cardholder",
        )
        return RuStoreBillingAssistant(ruStoreBillingClient)
    }
}
