package my.cardholder.cloud

import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
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
        const val FILE_NAME = "data.txt"
        const val MIME_TYPE_TEXT = "text/plain"
    }

    private val googleDrive
        get() = googleCredentialWrapper.getCredential(setOf(DriveScopes.DRIVE_APPDATA))
            ?.let { credential ->
                Drive.Builder(netHttpTransport, gsonFactory, credential)
                    .setApplicationName(BuildConfig.APP_NAME)
                    .build()
            }

    override fun downloadAppData(): Result<String> = runCatching {
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

    override fun uploadAppData(data: String): Result<Unit> = runCatching {
        googleDrive ?: throw IOException("Google Drive is not initialized")
        val driveFile = File().apply {
            setName(FILE_NAME)
            setParents(listOf(APP_DATA_FOLDER))
        }
        val driveFileContent = InputStreamContent(MIME_TYPE_TEXT, data.byteInputStream())
        googleDrive?.apply {
            getAppDataFolderFiles()
                .forEach { file -> files().delete(file.id).execute() }
            files().create(driveFile, driveFileContent).setFields("id").execute()
        }
    }

    private fun Drive.getAppDataFolderFiles(): List<File> {
        return files().list()
            .setSpaces(APP_DATA_FOLDER)
            .execute()?.files.orEmpty()
    }
}
