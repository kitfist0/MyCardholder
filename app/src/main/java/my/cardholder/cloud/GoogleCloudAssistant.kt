package my.cardholder.cloud

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

class GoogleCloudAssistant(
    private val googleCredentialWrapper: GoogleCredentialWrapper,
    private val gsonFactory: GsonFactory,
    private val netHttpTransport: NetHttpTransport,
) : CloudAssistant {

    private companion object {
        const val APP_DATA_FOLDER = "appDataFolder"
        const val MIME_TYPE_TEXT = "text/plain"
    }

    override suspend fun downloadFiles() = withContext(Dispatchers.IO) {
        runCatching {
            val drive = getDriveOrThrow()
            drive.getAppDataFolderFiles().map { file ->
                val outputStream = ByteArrayOutputStream()
                drive.files().get(file.id).executeMediaAndDownloadTo(outputStream)
                file.name to String(outputStream.toByteArray())
            }
        }
    }

    override suspend fun uploadFiles(namesAndContents: List<Pair<String, String>>) = withContext(Dispatchers.IO) {
        runCatching {
            val drive = getDriveOrThrow()

            // Delete old files
            val names = namesAndContents.map { it.first }
            drive.getAppDataFolderFiles()
                .filter { names.contains(it.name) }
                .forEach { file -> drive.files().delete(file.id).execute() }

            // Upload new files
            namesAndContents.forEach { nameAndContent ->
                val (name, content) = nameAndContent
                val driveFile = File().apply {
                    setName(name)
                    setParents(listOf(APP_DATA_FOLDER))
                }
                val driveFileContent = InputStreamContent(MIME_TYPE_TEXT, content.byteInputStream())
                drive.files().create(driveFile, driveFileContent).setFields("id").execute()
            }
        }
    }

    private fun Drive.getAppDataFolderFiles(): List<File> {
        return files().list()
            .setSpaces(APP_DATA_FOLDER)
            .execute()?.files.orEmpty()
    }

    private fun getDriveOrThrow(): Drive {
        return googleCredentialWrapper.getCredential()
            ?.let { credential ->
                Drive.Builder(netHttpTransport, gsonFactory, credential)
                    .setApplicationName(BuildConfig.APP_NAME)
                    .build()
            }
            ?: throw IOException("Google Drive is not initialized")
    }
}
