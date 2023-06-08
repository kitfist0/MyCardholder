package my.cardholder.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.android.billingclient.api.BillingClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import my.cardholder.data.source.local.AppDatabase
import my.cardholder.data.source.local.CardDao
import my.cardholder.data.source.local.CoffeeDao
import my.cardholder.data.source.local.LabelDao
import my.cardholder.data.source.local.LabelRefDao
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "cardholder.db")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    @Singleton
    fun provideCardDao(database: AppDatabase): CardDao {
        return database.cardDao()
    }

    @Provides
    @Singleton
    fun provideCoffeeDao(database: AppDatabase): CoffeeDao {
        return database.coffeeDao()
    }

    @Provides
    @Singleton
    fun provideLabelDao(database: AppDatabase): LabelDao {
        return database.labelDao()
    }

    @Provides
    @Singleton
    fun provideLabelRefDao(database: AppDatabase): LabelRefDao {
        return database.labelRefDao()
    }

    @Provides
    fun provideBillingClientBuilder(app: Application): BillingClient.Builder {
        return BillingClient.newBuilder(app)
    }

    @Provides
    fun provideFilesDir(app: Application): File {
        return app.filesDir
    }
}
