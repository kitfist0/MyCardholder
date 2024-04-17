package my.cardholder.di

import android.content.Context
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import my.cardholder.cloud.CloudAssistant
import my.cardholder.cloud.GoogleCloudAssistant
import my.cardholder.util.GoogleCredentialWrapper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CloudModule {
    @Provides
    @Singleton
    fun provideCloudAssistant(context: Context): CloudAssistant {
        return GoogleCloudAssistant(
            googleCredentialWrapper = GoogleCredentialWrapper(context),
            gsonFactory = GsonFactory.getDefaultInstance(),
            netHttpTransport = NetHttpTransport(),
        )
    }
}
