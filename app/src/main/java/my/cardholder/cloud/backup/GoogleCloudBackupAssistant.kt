package my.cardholder.cloud.backup

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

class GoogleCloudBackupAssistant(
    private val googleCredentialWrapper: GoogleCredentialWrapper,
    private val gsonFactory: GsonFactory,
    private val netHttpTransport: NetHttpTransport,
) : CloudBackupAssistant {

    private companion object {
        const val APP_DATA_FOLDER = "appDataFolder"
        const val BACKUP_FILE_NAME = "myBackup"
        const val FIELDS = "id,name,createdTime,modifiedTime"
        const val MIME_TYPE_TEXT = "text/plain"
    }

    override suspend fun getBackupVersion() = withContext(Dispatchers.IO) {
        runCatching {
            val drive = getDriveOrThrow()
            drive.getBackupFile()?.version ?: 0L
        }
    }

    override suspend fun deleteBackup() = withContext(Dispatchers.IO) {
        runCatching {
            val drive = getDriveOrThrow()
            drive.getAppDataFolderFiles()
                .forEach { file -> drive.files().delete(file.id).execute() }
        }
    }

    override suspend fun downloadBackupContent() = withContext(Dispatchers.IO) {
        runCatching {
            val drive = getDriveOrThrow()
            val file = drive.getBackupFile()
            file?.let {
                val outputStream = ByteArrayOutputStream()
                drive.files().get(it.id).executeMediaAndDownloadTo(outputStream)
                String(outputStream.toByteArray())
            }
        }
    }

    override suspend fun uploadBackupContent(content: String) = withContext(Dispatchers.IO) {
        runCatching {
            val drive = getDriveOrThrow()
            val inputStreamContent = InputStreamContent(MIME_TYPE_TEXT, content.byteInputStream())
            val prevFile = drive.getBackupFile()
            if (prevFile != null) {
                drive.files().update(prevFile.id, null, inputStreamContent)
                    .setFields(FIELDS).execute().version
            } else {
                val newFile = File()
                newFile.setName(BACKUP_FILE_NAME)
                newFile.setParents(listOf(APP_DATA_FOLDER))
                drive.files().create(newFile, inputStreamContent)
                    .setFields(FIELDS).execute().version
            }
        }
    }

    private fun Drive.getAppDataFolderFiles(): List<File> {
        return files().list()
            .setFields("files($FIELDS)")
            .setSpaces(APP_DATA_FOLDER)
            .execute()?.files.orEmpty()
    }

    private fun Drive.getBackupFile(): File? {
        return getAppDataFolderFiles()
            .firstOrNull { file -> file.name == BACKUP_FILE_NAME }
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
