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
        const val FIELDS = "id,name,createdTime,modifiedTime"
    }

    override suspend fun delete(fileName: String) = withContext(Dispatchers.IO) {
        runCatching {
            val drive = getDriveOrThrow()
            drive.getAppDataFolderFiles()
                .filter { it.name == fileName }
                .forEach { file -> drive.files().delete(file.id).execute() }
        }
    }

    override suspend fun download() = withContext(Dispatchers.IO) {
        runCatching {
            val drive = getDriveOrThrow()
            drive.getAppDataFolderFiles().map { file ->
                val outputStream = ByteArrayOutputStream()
                drive.files().get(file.id).executeMediaAndDownloadTo(outputStream)
                CloudFile(
                    fileNameAndContent = file.name to String(outputStream.toByteArray()),
                    timestamp = file.createdTime.value,
                )
            }
        }
    }

    override suspend fun upload(fileNameAndContent: FileNameAndContent) = withContext(Dispatchers.IO) {
        runCatching {
            val drive = getDriveOrThrow()

            // Delete old file
            val name = fileNameAndContent.getName()
            val previousFiles = drive.getAppDataFolderFiles()
                .filter { it.name == name }
                .sortedBy { it.modifiedTime.value }
            if (previousFiles.isNotEmpty()) {
                previousFiles.subList(0, previousFiles.lastIndex)
                    .forEach { file -> drive.files().delete(file.id).execute() }
            }
            val previousFile = previousFiles.lastOrNull()

            // Update previous file or upload new file
            val content = InputStreamContent(MIME_TYPE_TEXT, fileNameAndContent.getContent().byteInputStream())
            val file = if (previousFile != null) {
                drive.files().update(previousFile.id, null, content).setFields(FIELDS).execute()
            } else {
                val driveFile = File().apply {
                    setName(name)
                    setParents(listOf(APP_DATA_FOLDER))
                }
                drive.files().create(driveFile, content).setFields(FIELDS).execute()
            }
            CloudFile(
                fileNameAndContent = fileNameAndContent,
                timestamp = file.modifiedTime.value,
            )
        }
    }

    private fun Drive.getAppDataFolderFiles(): List<File> {
        return files().list()
            .setFields("files($FIELDS)")
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
