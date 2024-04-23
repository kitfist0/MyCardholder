package my.cardholder.di

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.DriveScopes
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

    private const val DRIVE_SCOPE = DriveScopes.DRIVE_APPDATA

    @Provides
    fun provideGoogleSignInClient(context: Context): GoogleSignInClient {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DRIVE_SCOPE))
            .build()
        return GoogleSignIn.getClient(context, googleSignInOptions)
    }

    @Provides
    @Singleton
    fun provideCloudAssistant(context: Context): CloudAssistant {
        return GoogleCloudAssistant(
            googleCredentialWrapper = GoogleCredentialWrapper(context, setOf(DRIVE_SCOPE)),
            gsonFactory = GsonFactory.getDefaultInstance(),
            netHttpTransport = NetHttpTransport(),
        )
    }
}
