package my.cardholder.billing

import android.content.Context
import com.android.billingclient.api.BillingClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface GooglePlayBillingModule {

    @Binds
    @Singleton
    fun bindPurchasedProductsProvider(assistant: GooglePlayBillingAssistant): PurchasedProductsProvider


    companion object {
        @Provides
        @Singleton
        fun provideGooglePlayBillingAssistant(
            context: Context
        ): GooglePlayBillingAssistant {
            return GooglePlayBillingAssistant(BillingClient.newBuilder(context))
        }
    }
}
