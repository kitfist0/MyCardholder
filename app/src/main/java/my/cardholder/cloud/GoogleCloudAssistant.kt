package my.cardholder.cloud

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.cardholder.BuildConfig
import my.cardholder.util.GoogleCredentialWrapper
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GoogleCloudAssistant(
    private val googleCredentialWrapper: GoogleCredentialWrapper,
    private val googleSignInClient: GoogleSignInClient,
    private val gsonFactory: GsonFactory,
    private val netHttpTransport: NetHttpTransport,
) : CloudAssistant {

    private companion object {
        const val APP_DATA_FOLDER = "appDataFolder"
        const val FILE_NAME = "data.txt"
        const val MIME_TYPE_TEXT = "text/plain"
    }

    private val googleDrive
        get() = googleCredentialWrapper.getCredential()
            ?.let { credential ->
                Drive.Builder(netHttpTransport, gsonFactory, credential)
                    .setApplicationName(BuildConfig.APP_NAME)
                    .build()
            }

    override val isCloudAvailable: Boolean
        get() = googleCredentialWrapper.getCredential() != null

    override val signInIntent: Intent
        get() = googleSignInClient.signInIntent

    override suspend fun downloadAppData(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            googleDrive ?: throw IOException("Google Drive is not initialized")
            var content = ""
            googleDrive?.apply {
                getAppDataFolderFiles().firstOrNull()
                    ?.let { file ->
                        val outputStream = ByteArrayOutputStream()
                        files().get(file.id).executeMediaAndDownloadTo(outputStream)
                        content = String(outputStream.toByteArray())
                    }
            }
            content
        }
    }

    override suspend fun uploadAppData(data: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            googleDrive ?: throw IOException("Google Drive is not initialized")
            val driveFile = File().apply {
                setName(FILE_NAME)
                setParents(listOf(APP_DATA_FOLDER))
            }
            val driveFileContent = InputStreamContent(MIME_TYPE_TEXT, data.byteInputStream())
            googleDrive?.run {
                getAppDataFolderFiles()
                    .forEach { file -> files().delete(file.id).execute() }
                files().create(driveFile, driveFileContent).setFields("id").execute()
            }
            Unit
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return suspendCoroutine { continuation ->
            googleSignInClient.signOut()
                .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }
    }

    private fun Drive.getAppDataFolderFiles(): List<File> {
        return files().list()
            .setSpaces(APP_DATA_FOLDER)
            .execute()?.files.orEmpty()
    }
}
