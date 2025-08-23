package my.cardholder.di

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.DriveScopes
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthSdk
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import my.cardholder.cloud.google.GoogleCloudBackupAssistant
import my.cardholder.cloud.google.GoogleCloudSignInAssistant
import my.cardholder.cloud.yandex.YandexCloudBackupAssistant
import my.cardholder.cloud.yandex.YandexCloudSignInAssistant
import my.cardholder.cloud.yandex.YandexDiskRestApi
import my.cardholder.cloud.yandex.YandexPreferences
import my.cardholder.util.GoogleCredentialWrapper
import my.cardholder.util.JwtTokenDecoder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CloudModule {

    private const val DRIVE_SCOPE = DriveScopes.DRIVE_APPDATA
    private const val OK_HTTP_TIMEOUT_IN_SECONDS = 30L

    @Provides
    fun provideGoogleSignInClient(context: Context): GoogleSignInClient {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DRIVE_SCOPE))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, googleSignInOptions)
    }

    @Provides
    @Singleton
    fun provideGoogleCloudAssistant(context: Context): GoogleCloudBackupAssistant {
        return GoogleCloudBackupAssistant(
            googleCredentialWrapper = GoogleCredentialWrapper(context, setOf(DRIVE_SCOPE)),
            gsonFactory = GsonFactory.getDefaultInstance(),
            netHttpTransport = NetHttpTransport(),
        )
    }

    @Provides
    @Singleton
    fun provideGoogleCloudSignInAssistant(
        context: Context,
        googleSignInClient: GoogleSignInClient,
    ): GoogleCloudSignInAssistant {
        return GoogleCloudSignInAssistant(
            googleCredentialWrapper = GoogleCredentialWrapper(context, setOf(DRIVE_SCOPE)),
            googleSignInClient = googleSignInClient,
        )
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(OK_HTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            .readTimeout(OK_HTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(OK_HTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            .followSslRedirects(true)
            .followRedirects(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideYandexDiskRestApi(okHttpClient: OkHttpClient): YandexDiskRestApi {
        return Retrofit.Builder()
            .baseUrl(YandexDiskRestApi.Companion.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YandexDiskRestApi::class.java)
    }

    @Provides
    @Singleton
    fun provideYandexCloudAssistant(
        okHttpClient: OkHttpClient,
        yandexDiskRestApi: YandexDiskRestApi,
        yandexPreferences: YandexPreferences,
    ): YandexCloudBackupAssistant {
        return YandexCloudBackupAssistant(
            okHttpClient = okHttpClient,
            yandexDiskRestApi = yandexDiskRestApi,
            yandexPreferences = yandexPreferences,
        )
    }

    @Provides
    @Singleton
    fun provideYandexCloudSignInAssistant(
        context: Context,
        yandexPreferences: YandexPreferences,
    ): YandexCloudSignInAssistant {
        return YandexCloudSignInAssistant(
            context = context,
            jwtTokenDecoder = JwtTokenDecoder(),
            yandexAuthSdk = YandexAuthSdk.Companion.create(YandexAuthOptions(context)),
            yandexPreferences = yandexPreferences,
        )
    }
}
