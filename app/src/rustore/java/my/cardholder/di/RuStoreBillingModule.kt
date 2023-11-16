package my.cardholder.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import my.cardholder.util.billing.PurchasedProductsProvider
import my.cardholder.util.billing.RuStoreBillingAssistant
import ru.rustore.sdk.billingclient.RuStoreBillingClientFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RuStoreBillingModule {

    @Binds
    fun bindPurchasedProductsProvider(assistant: RuStoreBillingAssistant): PurchasedProductsProvider

    companion object {
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
}
