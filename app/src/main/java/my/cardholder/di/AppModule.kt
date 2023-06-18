package my.cardholder.di

import android.app.Application
import android.content.Context
import com.android.billingclient.api.BillingClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import my.cardholder.util.PlayBillingWrapper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun providePlayBillingWrapper(context: Context): PlayBillingWrapper {
        val billingClientBuilder = BillingClient.newBuilder(context)
        return PlayBillingWrapper(billingClientBuilder)
    }
}
