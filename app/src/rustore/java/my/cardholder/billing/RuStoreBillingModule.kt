package my.cardholder.billing

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RuStoreBillingModule {

    @Binds
    fun bindPurchasedProductsProvider(assistant: RuStoreBillingAssistant): PurchasedProductsProvider

    companion object {
        @Provides
        @Singleton
        fun provideRuStoreBillingAssistant(): RuStoreBillingAssistant {
            return RuStoreBillingAssistant()
        }
    }
}
